<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { getAiResponse } from "@/apis/ai";

// AI assistant: plain-English questions about the four report metrics.
const userInput = ref(""); // user input
const messages = ref([]); // chat history

const sendMessage = async () => {
  if (!userInput.value.trim()) {
    ElMessage.warning("Please enter a message");
    return;
  }

  // store the user input in messages.
  messages.value.push({ role: "user", content: userInput.value });

  try {
    const res = await getAiResponse(userInput.value);
    const aiText = res.data.text || "AI cannot understand your request";
    messages.value.push({ role: "ai", content: aiText });
  } catch (error) {
    console.error("AI call failed:", error);
    messages.value.push({
      role: "ai",
      content: "Failed to call AI interface. Please try again later.",
    });
  }

  // clear input field
  userInput.value = "";
};
</script>

<template>
  <div class="ai-chat-box">
    <div class="ai-chat-header">
      <div class="title">
        <div class="name">🤖 AI Chatbot</div>
        <div class="sub">
          Please ask these type of questions only: Number of contracts /
          Contract amount / Number of orders / Top 5 employees
        </div>
      </div>
    </div>

    <div class="messages">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role]"
      >
        <strong v-if="msg.role === 'user'">You：</strong>
        <strong v-else>AI：</strong>
        <span>{{ msg.content }}</span>
      </div>
    </div>

    <div class="input-area">
      <el-input
        v-model="userInput"
        placeholder="Please enter your message..."
        @keyup.enter="sendMessage"
        style="width: 80%; margin-right: 10px"
      />
      <el-button type="primary" @click="sendMessage">Send</el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.ai-chat-box {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}

.messages {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 10px;
}

.message {
  margin: 5px 0;
}

.message.user {
  text-align: right;
  color: #409eff;
}

.message.ai {
  text-align: left;
  color: #67c23a;
}

.input-area {
  display: flex;
  align-items: center;
}

.ai-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.ai-chat-header .title .name {
  font-weight: 600;
  font-size: 16px;
}

.ai-chat-header .title .sub {
  font-size: 12px;
  color: #909399;
}
</style>
