#!/usr/bin/env python3
"""Run prompts from a CSV file through a local Ollama model and save answers as JSONL.

Requires Ollama running locally (https://ollama.com) with the target model
already pulled (e.g. `ollama pull llama3.2`). Uses only the Python standard
library — no pip install needed.

Usage:
    python ollama_batch.py --model llama3.2 --input questions.csv

    python ollama_batch.py --model qwen2.5:0.5b --input questions.csv \
        --prompt-column question --output result/custom_name.jsonl \
        --system "Answer in one short sentence." --temperature 0.2
"""

import argparse
import csv
import json
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
RESULT_DIR = SCRIPT_DIR / "result"


def parse_args():
    parser = argparse.ArgumentParser(description="Answer CSV prompts using a local Ollama model, output as JSONL.")
    parser.add_argument("--model", required=True, help="Ollama model name (e.g. llama3.2, qwen2.5:0.5b)")
    parser.add_argument("--input", required=True, type=Path, help="Path to the input CSV file")
    parser.add_argument(
        "--output",
        type=Path,
        default=None,
        help="Path to the output JSONL file (default: result/<input>_<model>_<timestamp>.jsonl)",
    )
    parser.add_argument(
        "--prompt-column",
        default="prompt",
        help="Name of the CSV column containing the prompt/question (default: prompt)",
    )
    parser.add_argument("--system", default=None, help="Optional system prompt sent with every request")
    parser.add_argument("--temperature", type=float, default=None, help="Optional sampling temperature")
    parser.add_argument(
        "--host",
        default="http://localhost:11434",
        help="Ollama API base URL (default: http://localhost:11434)",
    )
    parser.add_argument("--timeout", type=float, default=120.0, help="Per-request timeout in seconds (default: 120)")
    return parser.parse_args()


def call_ollama(host: str, model: str, prompt: str, system: str | None, temperature: float | None, timeout: float) -> dict:
    payload = {"model": model, "prompt": prompt, "stream": False}
    if system:
        payload["system"] = system
    if temperature is not None:
        payload["options"] = {"temperature": temperature}

    req = urllib.request.Request(
        f"{host.rstrip('/')}/api/generate",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def safe_model_name(model: str) -> str:
    return model.replace(":", "-").replace("/", "-")


def default_output_path(input_path: Path, model: str) -> Path:
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{input_path.stem}_{safe_model_name(model)}_{timestamp}.jsonl"
    return RESULT_DIR / filename


def main():
    args = parse_args()

    if not args.input.exists():
        print(f"Error: input file not found: {args.input}", file=sys.stderr)
        sys.exit(1)

    output_path = args.output or default_output_path(args.input, args.model)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with args.input.open("r", encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))

    if not rows:
        print("Error: input CSV has no data rows", file=sys.stderr)
        sys.exit(1)

    if args.prompt_column not in rows[0]:
        print(
            f"Error: column '{args.prompt_column}' not found in CSV. "
            f"Available columns: {', '.join(rows[0].keys())}",
            file=sys.stderr,
        )
        sys.exit(1)

    total = len(rows)
    ok_count = 0
    err_count = 0

    with output_path.open("w", encoding="utf-8") as out_f:
        for i, row in enumerate(rows, start=1):
            prompt = (row.get(args.prompt_column) or "").strip()
            print(f"[{i}/{total}] ", end="", flush=True)

            if not prompt:
                print("skipped (empty prompt)")
                continue

            record = {
                "row_index": i,
                "model": args.model,
                "prompt_column": args.prompt_column,
                "input": row,
                "timestamp": datetime.now(timezone.utc).isoformat(),
            }

            start = time.monotonic()
            try:
                result = call_ollama(args.host, args.model, prompt, args.system, args.temperature, args.timeout)
                record["response"] = result.get("response", "").strip()
                record["duration_seconds"] = round(time.monotonic() - start, 3)
                ok_count += 1
                print(f"ok ({record['duration_seconds']}s)")
            except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, OSError) as exc:
                record["response"] = None
                record["error"] = str(exc)
                record["duration_seconds"] = round(time.monotonic() - start, 3)
                err_count += 1
                print(f"error: {exc}")

            out_f.write(json.dumps(record, ensure_ascii=False) + "\n")
            out_f.flush()

    print()
    print(f"Done. {ok_count} succeeded, {err_count} failed, {total - ok_count - err_count} skipped.")
    print(f"Output written to {output_path}")


if __name__ == "__main__":
    main()
