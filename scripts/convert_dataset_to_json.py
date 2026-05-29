#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import unicodedata
from datetime import date, datetime
from pathlib import Path
from typing import Any

import pandas as pd

SUPPORTED_EXTENSIONS = (".csv", ".xls", ".xlsx")
NULL_TOKENS = {
    "", "unknown", "null", "none", "nan", "unknown_studio",
    "it looks like we don't have any soundtracks for this title yet.",
}

# Date columns are serialized as ISO xsd:date lexical values.
DATE_COLUMNS = {
    "released",
    "recorded",
    "release_date",
}

# Explicit renames for ambiguous/duplicate source headers.
EXPLICIT_COLUMN_RENAMES = {
    "written by": "movie_written_by",
}

# Columns that should be represented as JSON arrays for RML-ready structure.
ALWAYS_LIST_COLUMNS = {
    "written_by",
    "movie_written_by",
    "performed_by",
    "composed_by",
    "lyrics_by",
    "music_by",
    "songwriter",
    "producer",
    "music_genre",
    "movie_genre",
    "production_company",
    "production_country",
    "spoken_language",
    "original_language",
    "directed_by",
    "screenplay_by",
    "story_by",
    "produced_by",
    "starring",
    "movie_music_by",
}

# Slash can be a meaningful separator for these domain fields.
SLASH_SPLIT_COLUMNS = {
    "spoken_language",
    "original_language",
    "music_genre",
    "movie_genre",
    "production_country",
}

# Fields used as parts of IRIs in the mapping layer.
IRI_LIST_FIELDS = {
    "written_by",
    "movie_written_by",
    "performed_by",
    "composed_by",
    "lyrics_by",
    "music_by",
    "songwriter",
    "music_genre",
    "movie_genre",
    "production_company",
    "production_country",
    "spoken_language",
    "original_language",
    "directed_by",
    "screenplay_by",
    "story_by",
    "produced_by",
    "starring",
    "movie_music_by",
}

IRI_SCALAR_FIELDS = {
    "conducted_by",
    "courtesy_of",
    "under_license_from",
    "label",
    "studio",
}


def find_first_dataset(raw_dir: Path) -> Path:
    files = [
        path
        for path in raw_dir.iterdir()
        if path.is_file() and path.suffix.lower() in SUPPORTED_EXTENSIONS
    ]
    if not files:
        supported = ", ".join(SUPPORTED_EXTENSIONS)
        raise FileNotFoundError(
            f"No dataset file found in {raw_dir} with extensions: {supported}"
        )
    return sorted(files, key=lambda p: p.name.lower())[0]


def read_dataset(path: Path) -> pd.DataFrame:
    suffix = path.suffix.lower()
    if suffix == ".csv":
        try:
            return pd.read_csv(path)
        except UnicodeDecodeError:
            return pd.read_csv(path, encoding="latin-1")
    if suffix == ".xls":
        return pd.read_excel(path, engine="xlrd")
    if suffix == ".xlsx":
        try:
            return pd.read_excel(path, engine="openpyxl")
        except Exception:
            return pd.read_excel(path)
    raise ValueError(f"Unsupported file extension: {suffix}")


def clean_column_name(name: Any) -> str:
    cleaned = str(name).strip().lower()
    cleaned = re.sub(r"\s+", " ", cleaned)
    cleaned = cleaned.replace("-", "_").replace(" ", "_")
    cleaned = re.sub(r"_+", "_", cleaned).strip("_")
    return cleaned or "column"


def normalize_columns(columns: list[Any]) -> tuple[list[str], list[dict[str, str]]]:
    used: dict[str, int] = {}
    normalized: list[str] = []
    mapping: list[dict[str, str]] = []

    for original in columns:
        original_text = str(original).strip()
        explicit = EXPLICIT_COLUMN_RENAMES.get(original_text.lower())
        base = explicit if explicit else clean_column_name(original)
        used[base] = used.get(base, 0) + 1
        final_name = base if used[base] == 1 else f"{base}_{used[base]}"
        normalized.append(final_name)
        mapping.append({"original": str(original), "cleaned": final_name})

    return normalized, mapping


def is_null_like(value: Any) -> bool:
    if pd.isna(value):
        return True
    if isinstance(value, str):
        return value.strip().lower() in NULL_TOKENS
    return False


def normalize_scalar(value: Any) -> Any:
    if is_null_like(value):
        return None

    if isinstance(value, str):
        text = re.sub(r"\s+", " ", value.strip())
        lowered = text.lower()
        if lowered in NULL_TOKENS:
            return None
        if lowered == "true":
            return True
        if lowered == "false":
            return False
        return text

    # Keep numeric/boolean types as true JSON scalars.
    if isinstance(value, (bool, int, float)):
        # Convert float values like 120.0 to integer when exact.
        if isinstance(value, float) and value.is_integer():
            return int(value)
        return value

    text = str(value).strip()
    return text if text else None


def normalize_date(value: Any) -> str | None:
    if is_null_like(value):
        return None

    if isinstance(value, datetime):
        return value.date().isoformat()
    if isinstance(value, date):
        return value.isoformat()

    scalar = normalize_scalar(value)
    if scalar is None:
        return None

    text = str(scalar).strip()

    for fmt in ("%Y-%m-%d", "%Y-%m-%d %H:%M:%S", "%d/%m/%Y"):
        try:
            return datetime.strptime(text, fmt).date().isoformat()
        except ValueError:
            pass

    month_year = re.fullmatch(r"(\d{1,2})/(\d{4})", text)
    if month_year:
        month = int(month_year.group(1))
        year = int(month_year.group(2))
        if 1 <= month <= 12:
            return f"{year:04d}-{month:02d}-01"

    raise ValueError(
        f"Unsupported date value {text!r}; expected DD/MM/YYYY, MM/YYYY, or YYYY-MM-DD"
    )


def split_multi_value(value: Any, allow_slash: bool = False) -> list[Any]:
    if is_null_like(value):
        return []

    scalar = normalize_scalar(value)
    if scalar is None:
        return []

    if isinstance(scalar, (bool, int, float)):
        return [scalar]

    text = str(scalar)
    pattern = r"\s*[;,|/]\s*" if allow_slash else r"\s*[;,|]\s*"
    parts = re.split(pattern, text)

    cleaned_parts: list[str] = []
    seen: set[str] = set()
    for part in parts:
        token = re.sub(r"\s+", " ", part.strip())
        if not token or token.lower() in NULL_TOKENS:
            continue
        if token not in seen:
            seen.add(token)
            cleaned_parts.append(token)
    return cleaned_parts


def slugify_identifier(value: Any) -> str:
    normalized = normalize_scalar(value)
    if normalized is None:
        return ""

    text = str(normalized).strip().lower()
    if not text:
        return ""

    # Remove accents/diacritics to produce stable ASCII IRIs.
    text = unicodedata.normalize("NFKD", text)
    text = text.encode("ascii", "ignore").decode("ascii")
    text = text.replace("&", " and ")
    text = re.sub(r"[^\w\s-]", "", text)
    text = re.sub(r"[\s-]+", "_", text)
    text = re.sub(r"_+", "_", text).strip("_")
    return text


def slugify_list(values: Any) -> list[str]:
    if not isinstance(values, list):
        return []

    slugs: list[str] = []
    seen: set[str] = set()
    for value in values:
        slug = slugify_identifier(value)
        if not slug or slug in seen:
            continue
        seen.add(slug)
        slugs.append(slug)
    return slugs


def infer_multi_value_columns(df: pd.DataFrame) -> list[str]:
    inferred: list[str] = []
    for col in df.columns:
        if col in ALWAYS_LIST_COLUMNS:
            inferred.append(col)
            continue

        series = df[col].dropna()
        if series.empty:
            continue

        # Secondary inference for unexpected columns with obvious list separators.
        # This keeps the script adaptable if the dataset schema changes.
        text_series = series.astype(str).str.strip()
        text_series = text_series[
            ~text_series.str.lower().isin(NULL_TOKENS) & (text_series.str.len() > 0)
        ]
        if text_series.empty:
            continue

        delimiter_hits = text_series.str.contains(r"[,;|]").sum()
        ratio = delimiter_hits / len(text_series)
        median_len = text_series.str.len().median()

        if ratio >= 0.45 and median_len <= 80:
            inferred.append(col)

    # Keep deterministic order and remove duplicates.
    seen: set[str] = set()
    unique: list[str] = []
    for col in inferred:
        if col not in seen:
            seen.add(col)
            unique.append(col)
    return unique


def rows_to_json_records(
    df: pd.DataFrame, multi_value_columns: set[str]
) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []

    for row_index, row in df.iterrows():
        item: dict[str, Any] = {}
        for col in df.columns:
            value = row[col]
            if col in DATE_COLUMNS:
                item[col] = normalize_date(value)
            elif col in multi_value_columns:
                item[col] = split_multi_value(
                    value, allow_slash=col in SLASH_SPLIT_COLUMNS
                )
            else:
                item[col] = normalize_scalar(value)

        song_slug = slugify_identifier(item.get("song_name"))
        if not song_slug:
            song_slug = f"unknown_song_{int(row_index) + 1}"
        item["song_slug"] = song_slug

        movie_slug = slugify_identifier(item.get("movie_title"))
        if not movie_slug:
            movie_slug = f"unknown_film_{int(row_index) + 1}"
        item["movie_slug"] = movie_slug

        for field in IRI_LIST_FIELDS:
            item[f"{field}_slug"] = slugify_list(item.get(field))

        for field in IRI_SCALAR_FIELDS:
            slug = slugify_identifier(item.get(field))
            item[f"{field}_slug"] = slug if slug else None

        records.append(item)

    return records


def main() -> None:
    project_root = Path(__file__).resolve().parents[1]
    raw_dir = project_root / "data" / "raw"
    processed_dir = project_root / "data" / "processed"

    if not raw_dir.exists():
        raise FileNotFoundError(f"Missing input folder: {raw_dir}")

    input_path = find_first_dataset(raw_dir)
    df = read_dataset(input_path)

    normalized_columns, column_mapping = normalize_columns(list(df.columns))
    df.columns = normalized_columns

    multi_value_columns = infer_multi_value_columns(df)
    records = rows_to_json_records(df, set(multi_value_columns))

    processed_dir.mkdir(parents=True, exist_ok=True)
    dataset_out = processed_dir / "soundtracks_clean.json"
    profile_out = processed_dir / "columns_profile.json"

    with dataset_out.open("w", encoding="utf-8") as f:
        json.dump(records, f, ensure_ascii=False, indent=2)

    profile = {
        "source_file": str(input_path.relative_to(project_root).as_posix()),
        "rows": len(df),
        "columns_original_count": len(column_mapping),
        "columns_cleaned_count": len(df.columns),
        "column_mapping": column_mapping,
        "multi_value_columns": multi_value_columns,
        "date_columns": sorted(DATE_COLUMNS),
        "null_tokens_handled": sorted(NULL_TOKENS),
        "output_files": [
            str(dataset_out.relative_to(project_root).as_posix()),
            str(profile_out.relative_to(project_root).as_posix()),
        ],
    }

    with profile_out.open("w", encoding="utf-8") as f:
        json.dump(profile, f, ensure_ascii=False, indent=2)

    print(f"Input: {input_path.relative_to(project_root)}")
    print(f"Rows: {len(df)}")
    print(f"Multi-value columns: {len(multi_value_columns)}")
    print(f"Output: {dataset_out.relative_to(project_root)}")
    print(f"Profile: {profile_out.relative_to(project_root)}")


if __name__ == "__main__":
    main()
