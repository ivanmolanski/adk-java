"""Application scheduler for periodic tasks.

Uses APScheduler's AsyncIOScheduler to trigger:
 - Daily competitor scraping via Apify `ScrapingOrchestrator`
 - Daily email digest using `EmailDispatcher`

Environment Variables:
    ENABLE_SCHEDULER=1 -> start scheduler automatically at app startup (caller responsibility)

NOTE:
    In horizontally scaled deployments (multiple replicas / serverless), ensure ONLY one
    instance enables the scheduler to avoid duplicate scraping & emails. This can be done
    via leader election or setting ENABLE_SCHEDULER on a single maintenance pod.
"""

from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.cron import CronTrigger
import logging
import os
from fastapi import FastAPI

from app.models.database import get_session, async_session_maker  # type: ignore
from app.api.viral import send_daily_digest  # trigger_scraping now legacy alias
from app.agents.scraping_orchestrator import ScrapingOrchestrator
from app.pipeline import process_posts

logger = logging.getLogger(__name__)

_scheduler: AsyncIOScheduler | None = None


async def _run_scrape(app: FastAPI):
    logger.info('[SCHEDULER] Initiating scheduled scraping run (Apify)')
    try:
        orchestrator = ScrapingOrchestrator()
        items = await orchestrator.scrape(include_tiktok=False)
        if items:
            async with async_session_maker() as session:  # type: ignore
                result = await process_posts(session=session, posts=items, refine=True)
                logger.info('[SCHEDULER] Scrape pipeline result: %s', result)
        if hasattr(app.state, 'metrics'):
            m = app.state.metrics
            m['scrape_runs'] = m.get('scrape_runs', 0) + 1
            m['scrape_items_collected'] = m.get('scrape_items_collected', 0) + len(items)
    except Exception as e:  # noqa: BLE001
        logger.exception('Scheduled scraping failed: %s', e)
        if hasattr(app.state, 'metrics'):
            try:
                app.state.metrics['scrape_failures'] = app.state.metrics.get('scrape_failures', 0) + 1
            except Exception:  # noqa: BLE001
                pass


async def _run_digest(app: FastAPI):
    logger.info('[SCHEDULER] Initiating scheduled digest send')
    try:
        async for session in get_session():  # type: ignore
            await send_daily_digest(session=session)  # type: ignore[arg-type]
            break
        if hasattr(app.state, 'metrics'):
            app.state.metrics['digest_runs'] = app.state.metrics.get('digest_runs', 0) + 1
    except Exception as e:  # noqa: BLE001
        logger.exception('Scheduled digest failed: %s', e)


def init_scheduler(app: FastAPI):
    global _scheduler
    if _scheduler is not None:
        return _scheduler
    enabled = str(getattr(app.state, 'enable_scheduler', os.getenv('ENABLE_SCHEDULER', '0'))).lower() in {'1', 'true', 'yes', 'on'}
    if not enabled:
        logger.info('Scheduler initialization skipped (ENABLE_SCHEDULER not set)')
        return None
    scheduler = AsyncIOScheduler()
    scheduler.add_job(lambda: _run_scrape(app), CronTrigger(hour=13, minute=0))
    scheduler.add_job(lambda: _run_digest(app), CronTrigger(hour=13, minute=30))
    scheduler.start()
    logger.info('Scheduler started with daily scraping and digest jobs')
    _scheduler = scheduler
    return scheduler
