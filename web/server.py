"""
Project info server for the Course Registration Android app.

This is a native Android (Kotlin + Jetpack Compose) project. It cannot run
in a browser preview. This small server displays project information so the
Replit preview pane has something useful to show.
"""

import http.server
import os
import socketserver
from pathlib import Path

PORT = 5000
HOST = "0.0.0.0"
ROOT = Path(__file__).resolve().parent


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def end_headers(self):
        # Allow embedding in Replit preview iframe and disable caching for dev
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        super().end_headers()

    def log_message(self, fmt, *args):
        print("[web] " + fmt % args, flush=True)


class ReusableServer(socketserver.TCPServer):
    allow_reuse_address = True


def main() -> None:
    os.chdir(ROOT)
    print(f"[web] Serving project info page at http://{HOST}:{PORT}", flush=True)
    print(f"[web] Document root: {ROOT}", flush=True)
    with ReusableServer((HOST, PORT), Handler) as httpd:
        httpd.serve_forever()


if __name__ == "__main__":
    main()
