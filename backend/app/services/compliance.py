"""Lightweight compliance filtering for generated/chat content.

Rules:
 - Replace forbidden pharmaceutical brand term 'Botox' with approved synonyms.
 - Ensure no pricing statements (simple regex for currency or '$').
 - Provide list of applied transformations for auditing.
"""
from __future__ import annotations

import re
from typing import Dict, Any

FORBIDDEN_TERMS = [r"\b[Bb]otox\b"]
REPLACEMENT = "Neuromodulator"
PRICE_PATTERN = re.compile(r"\$\s?\d+|\b\d+\s?(?:USD|CAD|dollars)\b", re.IGNORECASE)


def apply_compliance(text: str) -> Dict[str, Any]:
  original = text
  transformed = text
  actions = []
  # Brand term replacement
  for pattern in FORBIDDEN_TERMS:
    if re.search(pattern, transformed):
      transformed = re.sub(pattern, REPLACEMENT, transformed)
      actions.append("replace_botox")
  # Pricing detection (flag but do not remove to retain context)
  pricing_flag = False
  if PRICE_PATTERN.search(transformed):
    pricing_flag = True
    actions.append("pricing_flag")
  return {
    "original": original,
    "compliant": transformed,
    "actions": actions,
    "pricing_flag": pricing_flag,
  }
