#!/usr/bin/env python3
"""Generate a Budget Companion backup JSON from local Excel files.

Source of truth:
- Mon_Budget (version 2).xlsx (expenses description + amount)

Optional enrichment:
- budget_organise.xlsx (period/category/notes/checkpoints)
- budget_v2_2.xlsx (payment hints)
"""

from __future__ import annotations

import json
import re
import unicodedata
import zipfile
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta
from difflib import SequenceMatcher
from pathlib import Path
from typing import Dict, List, Optional
import xml.etree.ElementTree as ET

NS_MAIN = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
NS_DOC_REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
NS_PKG_REL = "http://schemas.openxmlformats.org/package/2006/relationships"


def normalize_text(value: str) -> str:
    text = unicodedata.normalize("NFKD", value or "")
    text = "".join(ch for ch in text if not unicodedata.combining(ch))
    text = text.lower().strip()
    text = re.sub(r"[^a-z0-9]+", " ", text)
    return re.sub(r"\s+", " ", text).strip()


def parse_amount_minor(value: str) -> Optional[int]:
    raw = (value or "").strip().replace("\u00A0", " ").replace(" ", "")
    if not raw:
        return None
    if "," in raw and "." in raw:
        if raw.rfind(",") > raw.rfind("."):
            raw = raw.replace(".", "").replace(",", ".")
        else:
            raw = raw.replace(",", "")
    elif "," in raw:
        raw = raw.replace(",", ".")
    try:
        return int(round(float(raw) * 100))
    except ValueError:
        return None


def parse_excel_serial_to_epoch_millis(value: str) -> Optional[int]:
    raw = (value or "").strip()
    if not raw:
        return None
    try:
        serial = float(raw)
    except ValueError:
        return None
    dt = datetime(1899, 12, 30, tzinfo=UTC) + timedelta(days=serial)
    dt = dt.replace(hour=12, minute=0, second=0, microsecond=0)
    return int(dt.timestamp() * 1000)


MONTH_INDEX = {
    "jan": 1,
    "janv": 1,
    "january": 1,
    "feb": 2,
    "fev": 2,
    "fevr": 2,
    "february": 2,
    "mar": 3,
    "mars": 3,
    "april": 4,
    "avr": 4,
    "apr": 4,
    "mai": 5,
    "may": 5,
    "jun": 6,
    "juin": 6,
    "june": 6,
    "jul": 7,
    "juil": 7,
    "july": 7,
    "aou": 8,
    "aout": 8,
    "aug": 8,
    "august": 8,
    "sep": 9,
    "sept": 9,
    "september": 9,
    "oct": 10,
    "october": 10,
    "nov": 11,
    "november": 11,
    "dec": 12,
    "decembre": 12,
    "december": 12,
}


def parse_period_to_epoch_millis(value: str) -> Optional[int]:
    raw = (value or "").strip()
    if not raw:
        return None

    serial_epoch = parse_excel_serial_to_epoch_millis(raw)
    if serial_epoch is not None:
        return serial_epoch

    for fmt in ("%Y-%m-%d", "%d-%m-%Y", "%d/%m/%Y", "%d %b %Y", "%d %B %Y"):
        try:
            dt = datetime.strptime(raw, fmt).replace(tzinfo=UTC, hour=12)
            return int(dt.timestamp() * 1000)
        except ValueError:
            continue

    normalized = normalize_text(raw)
    m = re.match(r"^([a-z]+)\s+(\d{4})$", normalized)
    if m:
        month_key = m.group(1)
        year = int(m.group(2))
        month = MONTH_INDEX.get(month_key[:4]) or MONTH_INDEX.get(month_key[:3])
        if month:
            dt = datetime(year, month, 1, 12, 0, 0, tzinfo=UTC)
            return int(dt.timestamp() * 1000)
    return None


class XlsxReader:
    def __init__(self, path: Path):
        self.path = path
        self._zip = zipfile.ZipFile(path, "r")
        self.shared_strings = self._load_shared_strings()
        self.sheet_targets = self._load_sheet_targets()

    def close(self) -> None:
        self._zip.close()

    def _load_shared_strings(self) -> List[str]:
        if "xl/sharedStrings.xml" not in self._zip.namelist():
            return []
        root = ET.fromstring(self._zip.read("xl/sharedStrings.xml"))
        values: List[str] = []
        for si in root.findall(f"{{{NS_MAIN}}}si"):
            text = "".join((t.text or "") for t in si.findall(f".//{{{NS_MAIN}}}t"))
            values.append(text)
        return values

    def _load_sheet_targets(self) -> Dict[str, str]:
        workbook = ET.fromstring(self._zip.read("xl/workbook.xml"))
        rels = ET.fromstring(self._zip.read("xl/_rels/workbook.xml.rels"))
        rid_to_target = {
            node.attrib["Id"]: node.attrib["Target"]
            for node in rels.findall(f"{{{NS_PKG_REL}}}Relationship")
        }
        mapping: Dict[str, str] = {}
        sheets = workbook.find(f"{{{NS_MAIN}}}sheets")
        assert sheets is not None
        for sheet in sheets.findall(f"{{{NS_MAIN}}}sheet"):
            name = sheet.attrib["name"]
            rid = sheet.attrib[f"{{{NS_DOC_REL}}}id"]
            target = rid_to_target[rid]
            if not target.startswith("xl/"):
                target = f"xl/{target}"
            mapping[name] = target
        return mapping

    def find_sheet_name(self, *keywords: str) -> Optional[str]:
        normalized_keywords = [normalize_text(k) for k in keywords]
        for name in self.sheet_targets.keys():
            normalized_name = normalize_text(name)
            if all(k in normalized_name for k in normalized_keywords):
                return name
        return None

    def read_rows(self, sheet_name: str) -> List[Dict[str, str]]:
        target = self.sheet_targets[sheet_name]
        root = ET.fromstring(self._zip.read(target))
        sheet_data = root.find(f".//{{{NS_MAIN}}}sheetData")
        if sheet_data is None:
            return []
        rows: List[Dict[str, str]] = []
        for row in sheet_data.findall(f"{{{NS_MAIN}}}row"):
            cells: Dict[str, str] = {}
            for cell in row.findall(f"{{{NS_MAIN}}}c"):
                ref = cell.attrib.get("r", "")
                m = re.match(r"([A-Z]+)\d+", ref)
                if not m:
                    continue
                col = m.group(1)
                value = ""
                t = cell.attrib.get("t")
                v = cell.find(f"{{{NS_MAIN}}}v")
                if v is not None and v.text is not None:
                    raw = v.text
                    if t == "s":
                        if raw.isdigit():
                            idx = int(raw)
                            value = self.shared_strings[idx] if idx < len(self.shared_strings) else raw
                        else:
                            value = raw
                    else:
                        value = raw
                cells[col] = value
            rows.append(cells)
        return rows


@dataclass
class TruthExpense:
    description: str
    amount_minor: int


@dataclass
class EnrichedExpense:
    description: str
    amount_minor: int
    category: str
    occurred_at_epoch_millis: Optional[int]
    note: Optional[str]
    payment_method: Optional[str] = None


def map_payment(raw: Optional[str]) -> Optional[str]:
    key = normalize_text(raw or "")
    if not key:
        return None
    if "espece" in key or "cash" in key or "liquid" in key:
        return "LIQUIDE"
    if "virement" in key or "transfer" in key:
        return "VIREMENT"
    if "carte" in key or "card" in key or "tpe" in key:
        return "CARTE_TPE"
    return None


def load_truth_expenses(path: Path) -> List[TruthExpense]:
    reader = XlsxReader(path)
    try:
        sheet_name = reader.find_sheet_name("total")
        if sheet_name is None:
            raise RuntimeError("Could not find 'Total' sheet in source workbook")
        out: List[TruthExpense] = []
        for row in reader.read_rows(sheet_name):
            description = (row.get("D") or "").strip()
            amount_minor = parse_amount_minor(row.get("E") or "")
            if description and amount_minor and amount_minor > 0:
                out.append(TruthExpense(description=description, amount_minor=amount_minor))
        return out
    finally:
        reader.close()


def load_payment_hints(path: Path) -> Dict[tuple[str, int], str]:
    reader = XlsxReader(path)
    try:
        sheet_name = reader.find_sheet_name("depenses")
        if sheet_name is None:
            return {}
        hints: Dict[tuple[str, int], str] = {}
        for row in reader.read_rows(sheet_name):
            description = (row.get("B") or "").strip()
            amount_minor = parse_amount_minor(row.get("D") or "")
            payment = map_payment(row.get("E"))
            if not description or amount_minor is None or payment is None:
                continue
            hints[(normalize_text(description), amount_minor)] = payment
        return hints
    finally:
        reader.close()


def load_enriched_expenses(path: Path, payment_hints: Dict[tuple[str, int], str]) -> List[EnrichedExpense]:
    reader = XlsxReader(path)
    try:
        sheet_name = reader.find_sheet_name("depenses", "detail")
        if sheet_name is None:
            raise RuntimeError("Could not find detailed expenses sheet in budget_organise workbook")
        out: List[EnrichedExpense] = []
        for row in reader.read_rows(sheet_name):
            description = (row.get("C") or "").strip()
            amount_minor = parse_amount_minor(row.get("D") or "")
            category = (row.get("E") or "").strip()
            occurred = parse_period_to_epoch_millis(row.get("B") or "")
            note = (row.get("F") or "").strip() or None
            if not description or amount_minor is None or amount_minor <= 0:
                continue
            payment = payment_hints.get((normalize_text(description), amount_minor))
            out.append(
                EnrichedExpense(
                    description=description,
                    amount_minor=amount_minor,
                    category=category or "Divers",
                    occurred_at_epoch_millis=occurred,
                    note=note,
                    payment_method=payment,
                ),
            )
        return out
    finally:
        reader.close()


def load_checkpoints(path: Path) -> List[dict]:
    reader = XlsxReader(path)
    try:
        sheet_name = reader.find_sheet_name("solde")
        if sheet_name is None:
            return []
        checkpoints: List[dict] = []
        for row in reader.read_rows(sheet_name):
            recorded = parse_period_to_epoch_millis(row.get("A") or "")
            bank_minor = parse_amount_minor(row.get("B") or "")
            cash_minor = parse_amount_minor(row.get("C") or "") or 0
            note = (row.get("D") or "").strip() or None
            if recorded is None or bank_minor is None:
                continue
            checkpoints.append(
                {
                    "recordedAtEpochMillis": recorded,
                    "bankBalanceMinor": bank_minor,
                    "cashBalanceMinor": cash_minor,
                    "note": note,
                },
            )
        return checkpoints
    finally:
        reader.close()


def load_monthly_budget_minor(path: Path) -> int:
    reader = XlsxReader(path)
    try:
        sheet_name = reader.find_sheet_name("recapitulatif")
        if sheet_name is None:
            return 0
        rows = reader.read_rows(sheet_name)
        for row in rows:
            amount_minor = parse_amount_minor(row.get("E") or "")
            if amount_minor and 10_000 <= amount_minor <= 2_000_000:
                return amount_minor
        return 0
    finally:
        reader.close()


def similarity(a: str, b: str) -> float:
    if not a or not b:
        return 0.0
    if a == b:
        return 1.0
    if a in b or b in a:
        return 0.85
    return SequenceMatcher(None, a, b).ratio()


def match_truth_with_enrichment(
    truth: List[TruthExpense],
    enriched: List[EnrichedExpense],
) -> tuple[List[dict], float]:
    by_amount: Dict[int, List[int]] = {}
    for idx, item in enumerate(enriched):
        by_amount.setdefault(item.amount_minor, []).append(idx)

    used = set()
    fallback_base = datetime(2025, 1, 1, 12, 0, 0, tzinfo=UTC)
    merged: List[dict] = []
    matched_count = 0

    for index, source in enumerate(truth):
        key = normalize_text(source.description)
        candidate_indexes = [i for i in by_amount.get(source.amount_minor, []) if i not in used]

        best_idx: Optional[int] = None
        best_score = -1.0
        for ci in candidate_indexes:
            score = similarity(key, normalize_text(enriched[ci].description))
            if score > best_score:
                best_score = score
                best_idx = ci

        if best_idx is not None and best_score >= 0.45:
            used.add(best_idx)
            item = enriched[best_idx]
            matched_count += 1
            occurred = item.occurred_at_epoch_millis
            category = item.category or "Divers"
            note = item.note
            payment = item.payment_method or "CARTE_TPE"
        else:
            occurred = None
            category = "Divers"
            note = None
            payment = "CARTE_TPE"

        if occurred is None:
            occurred_dt = fallback_base + timedelta(days=index)
            occurred = int(occurred_dt.timestamp() * 1000)

        merged.append(
            {
                "description": source.description,
                "amountMinor": source.amount_minor,
                "occurredAtEpochMillis": occurred,
                "categoryName": category,
                "paymentMethod": payment,
                "note": note,
            },
        )

    coverage = matched_count / len(truth) if truth else 0.0
    return merged, coverage


def build_backup(
    merged_expenses: List[dict],
    checkpoints: List[dict],
    monthly_budget_minor: int,
) -> dict:
    categories = sorted({e["categoryName"] for e in merged_expenses})
    palette = [
        "#2E7D32",
        "#6D4C41",
        "#1565C0",
        "#8E24AA",
        "#EF6C00",
        "#C62828",
        "#00897B",
        "#3949AB",
        "#7B1FA2",
        "#455A64",
        "#5D4037",
    ]
    category_items = []
    category_id_by_name: Dict[str, int] = {}
    for idx, name in enumerate(categories, start=1):
        category_id_by_name[name] = idx
        category_items.append(
            {
                "id": idx,
                "name": name,
                "colorHex": palette[(idx - 1) % len(palette)],
                "sortOrder": idx - 1,
                "isActive": True,
                "monthlyBudgetMinor": 0,
            },
        )

    expenses = []
    for idx, entry in enumerate(merged_expenses, start=1):
        occurred = entry["occurredAtEpochMillis"]
        expenses.append(
            {
                "id": idx,
                "amountMinor": entry["amountMinor"],
                "occurredAtEpochMillis": occurred,
                "categoryId": category_id_by_name[entry["categoryName"]],
                "paymentMethod": entry["paymentMethod"],
                "merchantOrLabel": entry["description"],
                "note": entry["note"],
                "createdAtEpochMillis": occurred,
                "updatedAtEpochMillis": occurred,
                "source": "excel-import",
            },
        )

    checkpoints_items = []
    for idx, cp in enumerate(checkpoints, start=1):
        recorded = cp["recordedAtEpochMillis"]
        checkpoints_items.append(
            {
                "id": idx,
                "recordedAtEpochMillis": recorded,
                "bankBalanceMinor": cp["bankBalanceMinor"],
                "cashBalanceMinor": cp["cashBalanceMinor"],
                "note": cp["note"],
                "createdAtEpochMillis": recorded,
                "updatedAtEpochMillis": recorded,
            },
        )

    return {
        "schemaVersion": 2,
        "exportedAtEpochMillis": int(datetime.now(tz=UTC).timestamp() * 1000),
        "categories": category_items,
        "expenses": expenses,
        "checkpoints": checkpoints_items,
        "budgetConfig": {
            "monthlyBudgetMinor": monthly_budget_minor,
            "currencyCode": "TND",
            "monthStartDay": 1,
        },
        "templates": [],
        "recurringRules": [],
    }


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    truth_file = root / "Mon_Budget (version 2).xlsx"
    enriched_file = root / "budget_organise.xlsx"
    payment_file = root / "budget_v2_2.xlsx"

    if not truth_file.exists():
        raise FileNotFoundError(truth_file)

    truth = load_truth_expenses(truth_file)
    payment_hints = load_payment_hints(payment_file) if payment_file.exists() else {}
    enriched = load_enriched_expenses(enriched_file, payment_hints) if enriched_file.exists() else []
    checkpoints = load_checkpoints(enriched_file) if enriched_file.exists() else []
    monthly_budget_minor = load_monthly_budget_minor(truth_file)

    merged_expenses, coverage = match_truth_with_enrichment(truth, enriched)
    backup = build_backup(
        merged_expenses=merged_expenses,
        checkpoints=checkpoints,
        monthly_budget_minor=monthly_budget_minor,
    )

    output_backup = root / "backup_real_data.json"
    output_report = root / "backup_real_data_report.txt"
    output_backup.write_text(json.dumps(backup, ensure_ascii=False, indent=2), encoding="utf-8")

    report_lines = [
        f"truth_expenses={len(truth)}",
        f"enriched_expenses={len(enriched)}",
        f"match_coverage={coverage:.2%}",
        f"categories={len(backup['categories'])}",
        f"checkpoints={len(checkpoints)}",
        f"monthly_budget_minor={monthly_budget_minor}",
        f"output={output_backup.name}",
    ]
    output_report.write_text("\n".join(report_lines) + "\n", encoding="utf-8")
    print("\n".join(report_lines))


if __name__ == "__main__":
    main()
