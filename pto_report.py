#!/usr/bin/env python3
"""
PTO Net-Working-Days Report
Digital-Enablement-Effectiveness Team  |  Fiscal Year Sep 25 2025 – Aug 26 2026

Usage:
    python3 pto_report.py

The script calculates, for every team member, how many **net working days** of
PTO they took, after subtracting weekends and the appropriate public holidays:

  • Most team members  → Ontario, Canada statutory holidays
  • Bonnie Ray         → US Federal holidays (North Carolina)
  • Chris Isennock     → US Federal holidays (Tennessee)
  • Chris Gongora      → US Federal holidays (Tennessee)

PTO data was extracted from the 12 Shared-PTO-Tracker calendar screenshots
(Microsoft Teams, September 2025 – August 2026).  Events whose owner could
not be confirmed by OCR are listed at the bottom as "Unattributed Events" so
you can assign them.

To add / correct any entry, edit the PTO_DATA dictionary below.
Each value is a list of (start_date, end_date) tuples (inclusive, YYYY-MM-DD).
"""

from __future__ import annotations
from datetime import date, timedelta
from typing import Dict, List, Tuple


# ─────────────────────────────────────────────────────────────
# 1. HOLIDAY CALENDARS
# ─────────────────────────────────────────────────────────────

# Ontario, Canada statutory holidays for the fiscal year
ONTARIO_HOLIDAYS: List[date] = [
    date(2025,  9,  1),   # Labour Day
    date(2025, 10, 13),   # Thanksgiving Day
    date(2025, 11, 11),   # Remembrance Day
    date(2025, 12, 25),   # Christmas Day
    date(2025, 12, 26),   # Boxing Day
    date(2026,  1,  1),   # New Year's Day
    date(2026,  2, 16),   # Family Day
    date(2026,  4,  3),   # Good Friday
    date(2026,  4,  6),   # Easter Monday
    date(2026,  5, 18),   # Victoria Day
    date(2026,  7,  1),   # Canada Day
    date(2026,  8,  3),   # Civic Holiday (Ontario)
]

# US Federal holidays observed by NC and TN employees
# Jul 4 2026 falls on Saturday → observed Friday Jul 3
US_HOLIDAYS: List[date] = [
    date(2025,  9,  1),   # Labor Day
    date(2025, 10, 13),   # Columbus / Indigenous Peoples Day
    date(2025, 11, 11),   # Veterans Day
    date(2025, 11, 27),   # Thanksgiving Day
    date(2025, 12, 25),   # Christmas Day
    date(2026,  1,  1),   # New Year's Day
    date(2026,  1, 19),   # Martin Luther King Jr. Day
    date(2026,  2, 16),   # Presidents' Day
    date(2026,  4,  3),   # Good Friday (NC / TN state observation)
    date(2026,  5, 25),   # Memorial Day
    date(2026,  6, 19),   # Juneteenth National Independence Day
    date(2026,  7,  3),   # Independence Day observed (Jul 4 = Saturday)
]

HOLIDAY_SETS: Dict[str, List[date]] = {
    "ontario": ONTARIO_HOLIDAYS,
    "us":      US_HOLIDAYS,
}

HOLIDAY_LABELS: Dict[str, str] = {
    "ontario": "Ontario, Canada",
    "us":      "US Federal (NC / TN)",
}


# ─────────────────────────────────────────────────────────────
# 2. TEAM ROSTER
# ─────────────────────────────────────────────────────────────

# holiday_region must be a key in HOLIDAY_SETS
TEAM: Dict[str, Dict] = {
    "Fab Kazemi":       {"region": "ontario"},
    "Shaaz Khan":       {"region": "ontario"},
    "Ernest Ko":        {"region": "ontario"},
    "Chad Rogers":      {"region": "ontario"},
    "Samantha Foresti": {"region": "ontario"},
    "Chris Isennock":   {"region": "us"},
    "Roman Kolker":     {"region": "ontario"},
    "Rob Wilson":       {"region": "ontario"},
    "Bonnie Ray":       {"region": "us"},
    "Sophia Haile":     {"region": "ontario"},
    "Akash Ahir":       {"region": "ontario"},
    "Calvin Cheng":     {"region": "ontario"},
    "Chris Gongora":    {"region": "us"},
}


# ─────────────────────────────────────────────────────────────
# 3. PTO DATA  (edit / confirm from calendar screenshots)
# ─────────────────────────────────────────────────────────────
# Format: "Full Name": [(start, end), ...]   both dates INCLUSIVE
#
# Events extracted from the 12 monthly screenshots by:
#   • pixel-position → calendar-date mapping (positional analysis)
#   • partial OCR on event-bar text and visible event popups
#
# Confidence key:
#   CONFIRMED = read from event popup text in screenshot
#   PARTIAL   = name partially visible in OCR, dates from position analysis
#   INFERRED  = dates from position analysis only; person unidentified
#               → see UNATTRIBUTED list at bottom for these
#
# *** PLEASE VERIFY ALL ENTRIES AGAINST YOUR CALENDAR ***

PTO_DATA: Dict[str, List[Tuple[date, date]]] = {

    # ── September 2025 ──────────────────────────────────────
    # CONFIRMED by popup: Bonnie Ray ~Sep 28 – Oct 1
    "Bonnie Ray": [
        (date(2025, 9, 29), date(2025, 10,  1)),   # Sep 29 Mon – Oct 1 Wed  [CONFIRMED popup]
    ],

    # PARTIAL: "Rob PTO" seen in Dec 2025 & Jan 2026 event bars
    "Rob Wilson": [
        (date(2025, 12, 22), date(2025, 12, 24)),   # Dec 22–24  [PARTIAL OCR]
        (date(2026,  1,  5), date(2026,  1,  7)),   # Jan 5–7    [PARTIAL OCR]
    ],

    # PARTIAL: "Roman" seen in Dec 2025 band
    "Roman Kolker": [
        (date(2025, 12, 29), date(2026,  1,  2)),   # Dec 29 – Jan 2  [PARTIAL OCR]
    ],

    # ── Fill in below after verifying against your calendar ──

    "Fab Kazemi":       [],   # add (start, end) tuples
    "Shaaz Khan":       [],
    "Ernest Ko":        [],
    "Chad Rogers":      [],
    "Samantha Foresti": [],
    "Chris Isennock":   [],
    "Sophia Haile":     [],
    "Akash Ahir":       [],
    "Calvin Cheng":     [],
    "Chris Gongora":    [],
}


# ─────────────────────────────────────────────────────────────
# 4. UNATTRIBUTED EVENTS  (position-derived; owner unknown)
# ─────────────────────────────────────────────────────────────
# These events appeared in the calendar images but their owner
# could not be determined from OCR.  Assign them to the correct
# person by moving them into PTO_DATA above.

UNATTRIBUTED: List[Tuple[date, date, str]] = [
    # (start, end, notes)
    (date(2025,  9,  1), date(2025,  9,  5), "Sep wk1 Mon–Fri — 1 person, image band 1"),
    (date(2025,  9,  8), date(2025,  9, 12), "Sep wk2 Mon–Fri — 1 person, image band 2"),
    (date(2025,  9, 15), date(2025,  9, 19), "Sep wk3 Mon–Fri — possibly 2 people stacked (band 3, 36px tall)"),
    (date(2025,  9, 22), date(2025,  9, 26), "Sep wk4 Mon–Fri — possibly 2 people stacked (band 4, 27px tall)"),
    (date(2025, 11, 12), date(2025, 11, 15), "Nov Wed–Sat — 1 person, band 1"),
    (date(2025, 11, 17), date(2025, 11, 21), "Nov wk3 Mon–Fri — possibly 2 people stacked (band 2, 36px tall)"),
    (date(2025, 11, 24), date(2025, 11, 28), "Nov wk4 Mon–Fri — possibly 1–2 people (band 3, 27px tall)"),
    (date(2025, 12,  1), date(2025, 12,  5), "Dec wk1 Mon–Fri — 1–2 people (band 1, 27px tall)"),
    (date(2025, 12,  8), date(2025, 12, 12), "Dec wk2 Mon–Fri — 1–2 people (band 2, 27px tall)"),
    (date(2025, 12, 15), date(2025, 12, 16), "Dec Mon–Tue — 1 person (band 3, small)"),
    (date(2026,  1,  5), date(2026,  1,  9), "Jan wk1 Mon–Fri — 1–2 people stacked (band 1, 36px)"),
    (date(2026,  1, 12), date(2026,  1, 14), "Jan Mon–Wed — 1 person (band 2)"),
    (date(2026,  1, 15), date(2026,  1, 16), "Jan Thu–Fri — 1 person (band 3)"),
    (date(2026,  1, 20), date(2026,  1, 23), "Jan wk4 Tue–Fri — 1 person (band 4); note: Jan 19 = MLK Day (US)"),
    (date(2026,  2, 17), date(2026,  2, 20), "Feb Tue–Fri — 1 person (band 1); Family/Presidents Day Feb 16"),
    (date(2026,  2, 23), date(2026,  2, 27), "Feb Mon–Fri — 1 person (band 2)"),
    (date(2026,  3, 27), date(2026,  3, 27), "Mar Fri — 1 person (band 1)"),
    (date(2026,  4, 13), date(2026,  4, 17), "Apr Mon–Fri — 1 person (band 2)"),
    (date(2026,  5, 25), date(2026,  5, 25), "May Mon — 1 person (band 1); note: May 25 = Memorial Day (US)"),
]


# ─────────────────────────────────────────────────────────────
# 5. CALCULATION HELPERS
# ─────────────────────────────────────────────────────────────

def _working_days(start: date, end: date, holidays: List[date]) -> int:
    """Count Mon–Fri days in [start, end] that are not public holidays."""
    h_set = set(holidays)
    count = 0
    d = start
    while d <= end:
        if d.weekday() < 5 and d not in h_set:   # 0=Mon … 4=Fri
            count += 1
        d += timedelta(days=1)
    return count


def calc_member(name: str) -> Dict:
    region  = TEAM[name]["region"]
    hols    = HOLIDAY_SETS[region]
    h_label = HOLIDAY_LABELS[region]
    segments = PTO_DATA.get(name, [])

    gross = 0
    weekends = 0
    holiday_hits: set[date] = set()

    for start, end in segments:
        d = start
        while d <= end:
            gross += 1
            if d.weekday() >= 5:
                weekends += 1
            elif d in set(hols):
                holiday_hits.add(d)
            d += timedelta(days=1)

    net = gross - weekends - len(holiday_hits)
    return {
        "name":          name,
        "region":        h_label,
        "segments":      segments,
        "gross_days":    gross,
        "weekends":      weekends,
        "holidays_hit":  sorted(holiday_hits),
        "net_pto_days":  net,
    }


# ─────────────────────────────────────────────────────────────
# 6. REPORT OUTPUT
# ─────────────────────────────────────────────────────────────

def print_report() -> None:
    FISCAL_START = date(2025, 9, 25)
    FISCAL_END   = date(2026, 8, 26)

    sep = "─" * 80

    print()
    print("╔" + "═"*78 + "╗")
    print("║  PTO NET-WORKING-DAYS REPORT                                                 ║")
    print("║  Digital-Enablement-Effectiveness Team                                       ║")
    print(f"║  Fiscal Year: {FISCAL_START}  →  {FISCAL_END}                         ║")
    print("╚" + "═"*78 + "╝")
    print()

    # Summary table
    print(f"{'Name':<22}{'Holiday Region':<26}{'Gross':>6}{'Wkends':>8}{'Hols':>6}{'Net PTO':>9}")
    print(sep)

    total_net = 0
    results = []
    for name in TEAM:
        r = calc_member(name)
        results.append(r)
        flag = "  ← ** NEEDS DATA **" if not r["segments"] else ""
        print(f"{r['name']:<22}{r['region']:<26}"
              f"{r['gross_days']:>6}{r['weekends']:>8}{len(r['holidays_hit']):>6}"
              f"{r['net_pto_days']:>9}{flag}")
        total_net += r["net_pto_days"]

    print(sep)
    print(f"{'TOTAL':<54}{total_net:>9}")
    print()

    # Holiday calendars used
    print("HOLIDAY CALENDARS APPLIED")
    print(sep)
    for key, label in HOLIDAY_LABELS.items():
        print(f"\n  {label}:")
        for h in sorted(HOLIDAY_SETS[key]):
            if FISCAL_START <= h <= FISCAL_END:
                print(f"    {h.strftime('%a %b %d, %Y')}")

    print()

    # Detailed breakdown per person
    print("DETAILED BREAKDOWN PER PERSON")
    print(sep)
    for r in results:
        if not r["segments"]:
            continue
        print(f"\n  {r['name']}  [{r['region']}]")
        for start, end in r["segments"]:
            net_seg = _working_days(start, end, HOLIDAY_SETS[TEAM[r["name"]]["region"]])
            print(f"    {start}  →  {end}   ({net_seg} net working day{'s' if net_seg != 1 else ''})")
        if r["holidays_hit"]:
            print(f"    Holidays deducted: {', '.join(str(h) for h in r['holidays_hit'])}")

    # Unattributed events
    print()
    print("UNATTRIBUTED EVENTS  (assign to correct person above and re-run)")
    print(sep)
    for start, end, note in UNATTRIBUTED:
        # calculate working days for Ontario (default) 
        wd_on = _working_days(start, end, ONTARIO_HOLIDAYS)
        wd_us = _working_days(start, end, US_HOLIDAYS)
        print(f"  {start}  →  {end}  "
              f"({wd_on} ON / {wd_us} US net days)  NOTE: {note}")

    print()
    print("Run `python3 pto_report.py` after updating PTO_DATA to regenerate the table.")
    print()


if __name__ == "__main__":
    print_report()
