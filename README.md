# convert-file

A small Python script that converts a CSV file to JSON.

## Prerequisites

- Python 3 (no extra packages required — only uses the standard library).

  Verify with:

  ```bash
  py --version
  ```

  (If `py` isn't available, use `python` or `python3` instead in the commands below.)

## Run it

```bash
cd convert-file
py csv_to_json.py <input.csv> <output.json> [--indent N]
```

- `input.csv` — required. Path to the CSV file to convert.
- `output.json` — required. Path to write the JSON to.
- `--indent N` — optional. Indentation level for the JSON output (default: `2`).

Each CSV row becomes a JSON object, using the header row as keys.

## Example

A `sample.csv` is included to try it out immediately:

```bash
py csv_to_json.py sample.csv sample.json
```

This produces `sample.json`:

```json
[
  {
    "id": "1",
    "name": "Alice",
    "email": "alice@example.com"
  },
  ...
]
```

Custom output path and indentation:

```bash
py csv_to_json.py sample.csv output/data.json --indent 4
```
