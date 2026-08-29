OLLAMA_URL = "http://localhost:11434"

# CPU-only machines (no NVIDIA/AMD/Apple GPU — Intel integrated graphics
# does not get Ollama acceleration) will be materially faster on a
# smaller model. Options, roughly fastest to highest quality:
#   qwen2.5:0.5b  - fastest, noticeably rougher question quality
#   qwen2.5:1.5b  - good middle ground for CPU-only setups (current default)
#   qwen2.5:3b    - best quality, needs GPU to feel fast
# Remember to `ollama pull <model>` before switching.
OLLAMA_MODEL = "qwen2.5:3b"
