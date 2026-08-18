# ollama-batch-runner

A Python script that reads prompts from a CSV file, sends each one to a local
[Ollama](https://ollama.com) model, and writes the answers as JSONL under `result/`.

No third-party packages required — only the Python standard library (talks to
Ollama's local REST API directly over HTTP).

## Prerequisites

- [Ollama](https://ollama.com) installed and running locally
- At least one model pulled, e.g.:

  ```bash
  ollama pull llama3.2
  ```

- Python 3.10+

## Usage

```bash
python ollama_batch.py --model <model-name> --input <input.csv> [options]
```

| Flag | Required | Default | Description |
|---|---|---|---|
| `--model` | yes | — | Ollama model name (e.g. `llama3.2`, `qwen2.5:0.5b`) — this is the parameter you swap to change models |
| `--input` | yes | — | Path to the input CSV file |
| `--output` | no | `result/<input>_<model>_<timestamp>.jsonl` | Path to the output JSONL file |
| `--prompt-column` | no | `prompt` | Name of the CSV column containing the prompt/question |
| `--system` | no | none | Optional system prompt sent with every request |
| `--temperature` | no | Ollama's default | Optional sampling temperature |
| `--host` | no | `http://localhost:11434` | Ollama API base URL |
| `--timeout` | no | `120` | Per-request timeout, in seconds |

### Input CSV

Any CSV with a column holding the prompt text (default column name: `prompt`).
Extra columns are carried through into the output for traceability. Example
(`sample_questions.csv`):

```csv
id,prompt
1,What is the capital of France?
2,What is 12 + 30?
3,Name one primary color.
```

### Output JSONL

One JSON object per input row, written to `result/`:

```json
{"row_index": 1, "model": "qwen2.5:0.5b", "prompt_column": "prompt", "input": {"id": "1", "prompt": "What is the capital of France?"}, "timestamp": "2026-08-18T06:18:03.295532+00:00", "response": "The capital of France is Paris.", "duration_seconds": 15.661}
```

- `input` — the full original CSV row (so you can trace an answer back to its source data)
- `response` — the model's answer (`null` if the request failed)
- `error` — present only if the request failed (row is still written, not skipped)
- Empty-prompt rows are skipped entirely (not written to output)

## Example

```bash
ollama pull qwen2.5:0.5b
python ollama_batch.py --model qwen2.5:0.5b --input sample_questions.csv --temperature 0.2
```

Swap models without touching the script or CSV:

```bash
python ollama_batch.py --model llama3.2 --input sample_questions.csv
```

## Notes

- Runs prompts sequentially (one at a time) — matches how a single local Ollama
  instance processes requests. Runtime scales with number of rows × model speed.
- Failed requests (bad model name, Ollama not running, timeout, etc.) are logged
  per-row in the output with an `error` field rather than aborting the whole run.
