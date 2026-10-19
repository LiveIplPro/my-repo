import os
import time
from threading import Thread
from flask import Flask
import telebot

# 1. Flask वेब सर्वर सेटअप (Render को एक्टिव रखने के लिए)
app = Flask(__name__)

@app.route('/')
def home():
    return "Mera Telegram Bot Render Par 24/7 Live Hai!", 200

# 2. टेलीग्राम बॉट सेटअप
# यहाँ अपना असली टोकन डालें (उद्धरण चिह्नों "" के अंदर)
BOT_TOKEN = "8474712057:AAHAOpGfcm5fdoiwgyF9dYQyzaCH4_46J4g" 
bot = telebot.Telebot(BOT_TOKEN)

# जब कोई यूजर /start या /help भेजेगा, तो यह फंक्शन चलेगा
@bot.message_handler(commands=['start', 'help'])
def send_welcome(message):
    try:
        bot.reply_to(message, "नमस्ते! आपका टेलीग्राम बॉट अब Render क्लाउड पर 24/7 पूरी तरह काम कर रहा है। 🚀")
        print(f"Success: Replied to Chat ID {message.chat.id}")
    except Exception as e:
        print(f"Error sending message: {e}")

# जब यूजर कोई भी सामान्य टेक्स्ट मैसेज भेजेगा
@bot.message_handler(func=lambda message: True)
def echo_all(message):
    try:
        bot.reply_to(message, f"आपने कहा: {message.text}")
    except Exception as e:
        print(f"Error in echo: {e}")

# बॉट को बिना रुके (Continuous Polling) चलाने का फंक्शन
def run_bot():
    print("Starting Telegram Bot Polling...")
    while True:
        try:
            # non_stop=True रखने से नेटवर्क एरर आने पर भी बॉट बंद नहीं होता
            bot.infinity_polling(timeout=10, long_polling_timeout=5)
        except Exception as e:
            print(f"Bot polling encountered an error: {e}")
            time.sleep(5)

if __name__ == "__main__":
    # 3. बॉट को एक अलग थ्रेड में शुरू करना
    bot_thread = Thread(target=run_bot)
    bot_thread.daemon = True
    bot_thread.start()
    
    # 4. वेब सर्वर को चालू करना
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)
