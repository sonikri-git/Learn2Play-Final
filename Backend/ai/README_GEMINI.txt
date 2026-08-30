# Learn2Play AI - Gemini version

This folder uses the Gemini API instead of Ollama.

## Required environment variable

GEMINI_API_KEY=your_key_here

Optional:

GEMINI_MODEL=gemini-3.5-flash-lite

## Local test

Windows PowerShell:

$env:GEMINI_API_KEY="your_key_here"
python quizgen.py yourfile.pdf --type short

## Render

Add GEMINI_API_KEY under Render Dashboard -> Backend service -> Environment.

Do not put the API key in config.py or commit it to GitHub.

The existing Spring Boot ProcessBuilder can continue running:

python ai/quizgen.py ...

No Ollama installation or local model is required.
