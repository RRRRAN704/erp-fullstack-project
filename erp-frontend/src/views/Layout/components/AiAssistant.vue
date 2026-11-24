<script setup>
import { nextTick, ref, watch } from "vue";
import { getAiResponse } from "@/apis/ai";
import { useUserStore } from "@/stores/user";

// AI assistant: a floating button on every employee page that opens a fixed-size chat panel.
// Messages scroll inside the panel, so the page itself never grows.

const SUGGESTIONS = [
  "How many contracts did we sign last month?",
  "What was the total contract value this year?",
  "How many consultation orders came in this week?",
  "Who were the top 5 employees last month?",
];

const userStore = useUserStore();
// History survives a page refresh (sessionStorage = this browser tab only), kept per user
const storageKey = `ai-chat-${userStore.userInfo?.id ?? "guest"}`;
const loadHistory = () => {
  try {
    return JSON.parse(sessionStorage.getItem(storageKey)) ?? [];
  } catch {
    return [];
  }
};

const open = ref(false);
const messages = ref(loadHistory()); // { role: "user" | "ai", content, error? }
const input = ref("");
const thinking = ref(false);
const listRef = ref(null);

watch(messages, (value) => sessionStorage.setItem(storageKey, JSON.stringify(value)), { deep: true });

const scrollToBottom = async () => {
  await nextTick();
  if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight;
};
watch(open, (isOpen) => isOpen && scrollToBottom());

const send = async (text = input.value) => {
  const question = text.trim();
  if (!question || thinking.value) return;

  messages.value.push({ role: "user", content: question });
  input.value = "";
  thinking.value = true;
  scrollToBottom();

  try {
    const res = await getAiResponse(question);
    // code 0 = business error with a readable message (e.g. the AI service is unavailable)
    if (res.code === 1) {
      messages.value.push({ role: "ai", content: res.data?.text || "Sorry, I couldn't understand that." });
    } else {
      messages.value.push({ role: "ai", content: res.msg || "Something went wrong.", error: true });
    }
  } catch {
    messages.value.push({ role: "ai", content: "Couldn't reach the server. Please try again.", error: true });
  } finally {
    thinking.value = false;
    scrollToBottom();
  }
};

const clearChat = () => {
  messages.value = [];
};
</script>

<template>
  <!-- Floating button -->
  <button v-show="!open" class="ai-fab" aria-label="Open AI assistant" @click="open = true">
    <span class="ai-fab-icon">✦</span>
    <span class="ai-fab-label">Ask AI</span>
  </button>

  <!-- Chat panel -->
  <section v-if="open" class="ai-panel" aria-label="AI assistant">
    <header class="ai-header">
      <div>
        <div class="ai-title">AI Assistant</div>
        <div class="ai-subtitle">Ask about contracts, orders and top employees</div>
      </div>
      <div class="ai-header-actions">
        <button class="ai-icon-btn" title="Clear chat" :disabled="!messages.length" @click="clearChat">
          Clear
        </button>
        <button class="ai-icon-btn ai-close" aria-label="Close" @click="open = false">✕</button>
      </div>
    </header>

    <div ref="listRef" class="ai-messages">
      <!-- Empty state: example questions -->
      <div v-if="!messages.length" class="ai-empty">
        <p>Try one of these:</p>
        <button v-for="s in SUGGESTIONS" :key="s" class="ai-suggestion" @click="send(s)">
          {{ s }}
        </button>
      </div>

      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['ai-bubble', msg.role, { error: msg.error }]"
      >
        {{ msg.content }}
      </div>

      <div v-if="thinking" class="ai-bubble ai thinking" aria-label="Thinking">
        <span></span><span></span><span></span>
      </div>
    </div>

    <form class="ai-input" @submit.prevent="send()">
      <el-input
        v-model="input"
        placeholder="Ask a question…"
        :disabled="thinking"
        maxlength="500"
      />
      <el-button type="primary" native-type="submit" :loading="thinking" :disabled="!input.trim()">
        Send
      </el-button>
    </form>
  </section>
</template>

<style scoped>
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  border: none;
  border-radius: 999px;
  background: #409eff;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
  transition: transform 0.15s ease;
}

.ai-fab:hover {
  transform: translateY(-2px);
}

.ai-fab-icon {
  font-size: 18px;
}

.ai-panel {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
  width: 380px;
  height: 540px;
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #409eff;
  color: #fff;
}

.ai-title {
  font-weight: 600;
  font-size: 16px;
}

.ai-subtitle {
  font-size: 12px;
  opacity: 0.85;
}

.ai-header-actions {
  display: flex;
  gap: 4px;
}

.ai-icon-btn {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: #fff;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 13px;
  cursor: pointer;
}

.ai-icon-btn:disabled {
  opacity: 0.4;
  cursor: default;
}

/* Only this part scrolls */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #f7f8fa;
}

.ai-empty {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #909399;
  font-size: 13px;
}

.ai-suggestion {
  text-align: left;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  color: #303133;
  font-size: 13px;
  cursor: pointer;
}

.ai-suggestion:hover {
  border-color: #409eff;
  color: #409eff;
}

.ai-bubble {
  max-width: 85%;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-bubble.user {
  align-self: flex-end;
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-bubble.ai {
  align-self: flex-start;
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 4px;
}

.ai-bubble.ai.error {
  background: #fef0f0;
  border-color: #fde2e2;
  color: #c45656;
}

/* three bouncing dots */
.ai-bubble.thinking {
  display: flex;
  gap: 4px;
  padding: 14px 16px;
}

.ai-bubble.thinking span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: ai-bounce 1.2s infinite ease-in-out;
}

.ai-bubble.thinking span:nth-child(2) {
  animation-delay: 0.15s;
}

.ai-bubble.thinking span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes ai-bounce {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  40% {
    transform: translateY(-5px);
    opacity: 1;
  }
}

.ai-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #ebeef5;
}

/* Phones: the panel takes the whole screen; the button shows only its icon */
@media (max-width: 767px) {
  .ai-panel {
    inset: 0;
    width: 100%;
    height: 100%;
    max-height: none;
    border-radius: 0;
  }

  .ai-fab {
    right: 16px;
    bottom: 16px;
    padding: 14px 16px;
  }

  .ai-fab-label {
    display: none;
  }
}
</style>
