"""Execute the built consumer page in a headless Chromium browser."""
import argparse
import functools
import html
import http.server
import json
from pathlib import Path
import re
import subprocess
import threading
import uuid

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--browser", required=True, help="Chromium/Chrome/Edge executable")
args = parser.parse_args()
root = Path(__file__).resolve().parent.parent
dist = root / "verification/web/dist"
if not (dist / "index.html").is_file():
    parser.error("Build verification/web first")
profile = (root / "build" / f"browser-profile-{uuid.uuid4()}").resolve()
if not profile.is_relative_to((root / "build").resolve()):
    raise RuntimeError("Unexpected verification profile location")

class QuietHandler(http.server.SimpleHTTPRequestHandler):
    def log_message(self, *_):
        pass

with http.server.ThreadingHTTPServer(("127.0.0.1", 0), functools.partial(QuietHandler, directory=str(dist))) as server:
    threading.Thread(target=server.serve_forever, daemon=True).start()
    try:
        result = subprocess.run([
            args.browser, "--headless", "--disable-gpu", "--no-first-run", "--no-default-browser-check",
            f"--user-data-dir={profile}", "--virtual-time-budget=10000", "--dump-dom",
            f"http://127.0.0.1:{server.server_port}/",
        ], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=45, check=True)
    finally:
        server.shutdown()

match = re.search(r'<pre id="result">(.*?)</pre>', result.stdout, re.DOTALL)
if not match:
    raise RuntimeError("Browser did not render the verification result")
report = json.loads(html.unescape(match.group(1)))
if report.get("status") != "passed":
    raise RuntimeError(f"Browser verification failed: {report}")
output = root / "build/reports/browser-verification.json"
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
print(json.dumps(report, indent=2))
