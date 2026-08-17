#!/usr/bin/env python3
"""Convert a CSV file to JSON.

Usage:
    python csv_to_json.py input.csv output.json
"""

import argparse
import csv
import json
import sys
from pathlib import Path


def convert(input_path: Path, output_path: Path, indent: int) -> int:
    with input_path.open("r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        rows = list(reader)

    with output_path.open("w", encoding="utf-8") as f:
        json.dump(rows, f, indent=indent, ensure_ascii=False)
        f.write("\n")

    return len(rows)


def main():
    parser = argparse.ArgumentParser(description="Convert a CSV file to JSON.")
    parser.add_argument("input", type=Path, help="Path to the input CSV file")
    parser.add_argument("output", type=Path, help="Path to the output JSON file")
    parser.add_argument(
        "--indent",
        type=int,
        default=2,
        help="Indentation level for the JSON output (default: 2)",
    )
    args = parser.parse_args()

    if not args.input.exists():
        print(f"Error: input file not found: {args.input}", file=sys.stderr)
        sys.exit(1)

    count = convert(args.input, args.output, args.indent)
    print(f"Converted {count} row(s) from {args.input} to {args.output}")


if __name__ == "__main__":
    main()
