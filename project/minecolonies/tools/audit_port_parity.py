"""Read-only source/resource inventory and event-bridge audit against the pinned Forge baseline.

This identifies review candidates, not gameplay equivalence. It never executes baseline code.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
from collections import Counter


def source(path):
    return path.read_text(encoding="utf-8-sig", errors="replace")


def strip_comments(text):
    # Keep quoted strings intact while excluding comment-only TODOs and event constructors.
    return re.sub(r'("(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\')|//[^\n]*|/\*[\s\S]*?\*/',
                  lambda match: match[1] or "", text)


def audit(port, baseline):
    def inventory(root):
        return {str(file.relative_to(root)).replace("\\", "/"): file
                for folder in ("src/main/java", "src/api/java", "src/main/resources", "src/api/resources")
                for file in (root / folder).rglob("*") if file.is_file()}
    print("Inventory", flush=True)
    original, current = inventory(baseline), inventory(port)
    print(f"Compare {len(original)} original and {len(current)} current files", flush=True)
    same, changed = [], []
    for key in sorted(original.keys() & current.keys()):
        # Line ending changes alone are not a port difference.
        digest = lambda path: hashlib.sha256(path.read_bytes().replace(b"\r\n", b"\n")).hexdigest()
        (same if digest(original[key]) == digest(current[key]) else changed).append(key)
    print("Source/event analysis", flush=True)
    texts = {key: strip_comments(source(file)) for key, file in current.items() if key.endswith(".java")}
    production = {key: text for key, text in texts.items() if "/gametest/" not in key}
    all_code = "\n".join(production.values())
    constructors = Counter(name.split(".")[-1] for name in re.findall(r'\bnew\s+([\w.$]+)\s*\(', all_code))
    handlers = []
    for key, text in production.items():
        for match in re.finditer(r'@SubscribeEvent(?:\([^)]*\))?[\s\w@().,=]*?\b(?:public|private|protected)\s+(?:static\s+)?void\s+(\w+)\s*\(\s*(?:@[\w.]+\s+|final\s+)*([\w.$]+)\s+\w+\s*\)', text):
            event = match[2].split(".")[-1]
            creations = constructors[event]
            handlers.append({"file": key, "method": match[1], "event": match[2], "constructor_sites": creations,
                             "line": text[:match.start()].count("\n") + 1})
    adapters = [key for key in production if "/com/minecolonies/fabric/" in key or "/net/minecraftforge/" in key]
    literals = []
    for key in adapters:
        for match in re.finditer(r'\b(public|protected)[^\n{};]*?\s+(\w+)\s*\([^{};]*\)\s*\{\s*(?:return\s+(null|false|true|0|0\.0|ItemStack\.EMPTY);)?\s*\}', production[key]):
            literals.append({"file": key, "method": match[2], "constant": match[3],
                             "line": production[key][:match.start()].count("\n") + 1})
    return {"baseline": str(baseline), "port": str(port),
            "counts": {"baseline_files": len(original), "port_files": len(current), "identical": len(same),
                       "changed": len(changed), "missing": len(original.keys() - current.keys()),
                       "added": len(current.keys() - original.keys()), "adapters": len(adapters)},
            "missing": sorted(original.keys() - current.keys()), "changed": changed,
            "added": sorted(current.keys() - original.keys()),
            "handlers": handlers,
            "events_without_constructor_site": [event for event in handlers if not event["constructor_sites"]],
            "constant_adapter_methods": literals,
            "limits": ["Inventory excludes generated resource roots, which require a separate semantic comparison.",
                       "Event constructor and constant-method scans are conservative review candidates, not proof of missing behavior.",
                       "Identical source does not prove equivalent loader lifecycle, network, rendering or persistence."]}


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=Path, required=True)
    parser.add_argument("--baseline", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    result = audit(args.port, args.baseline)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"counts": result["counts"], "event_review": result["events_without_constructor_site"],
                      "constant_adapter_methods": result["constant_adapter_methods"]}, ensure_ascii=False, indent=2))
