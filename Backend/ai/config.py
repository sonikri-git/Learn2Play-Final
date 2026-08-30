import os

# Gemini API configuration.
# Set GEMINI_API_KEY in Render Environment Variables.
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "").strip()

# You can override this in Render with the GEMINI_MODEL environment variable.
GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash-lite").strip()

GEMINI_API_URL = (
    f"https://generativelanguage.googleapis.com/v1beta/models/"
    f"{GEMINI_MODEL}:generateContent"
)
