import json

log_path = r"C:\Users\sreen\.gemini\antigravity\brain\0074ec70-e87b-4145-9099-ce99aab72b1d\.system_generated\logs\transcript.jsonl"
with open(log_path, "r", encoding="utf-8", errors="ignore") as f:
    for line in f:
        if '"step_index": 9307' in line or '"step_index": 9306' in line:
            obj = json.loads(line)
            print("Step:", obj.get("step_index"))
            for tc in obj.get("tool_calls", []):
                print(tc)
