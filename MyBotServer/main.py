
import os
import time
import requests
from threading import Thread
from flask import Flask

app = Flask(__name__)

@app.route('/')
def home():
    return "Mera Telegram Bot 24/7 Live Hai!", 200

def handle_bot_logic():
    # अपना असली टेलीग्राम टोकन यहाँ डालें
    bot_token = "8474712057:AAHAOpGfcm5fdoiwgyF9dYQyzaCH4_46J4g"
    base_url = f"https://telegram.org{bot_token}"
    
    # यह ट्रैक रखने के लिए कि हम पुराना मैसेज दोबारा न पढ़ें
    last_update_id = 0 
    
    print("Bot background loop started successfully...")

    while True:
        try:
            # 1. टेलीग्राम सर्वर से नए मैसेजेस चेक करना (getUpdates)
            updates_url = f"{base_url}/getUpdates?offset={last_update_id + 1}&timeout=30"
            response = requests.get(updates_url, timeout=35)
            
            if response.status_code == 200:
                data = response.json()
                
                if "result" in data and len(data["result"]) > 0:
                    for update in data["result"]:
                        last_update_id = update["update_id"]
                        
                        # चेक करना कि क्या कोई टेक्स्ट मैसेज आया है
                        if "message" in update and "text" in update["message"]:
                            chat_id = update["message"]["chat"]["id"]
                            user_text = update["message"]["text"]
                            
                            print(f"Received message: {user_text} from Chat ID: {chat_id}")
                            
                            # 2. अगर यूजर ने /start भेजा है, तो उसका जवाब तैयार करना
                            if user_text == "/start":
                                reply_text = "नमस्ते! आपका टेलीग्राम बॉट अब Render सर्वर पर 24/7 लाइव काम कर रहा है। 👍"
                                
                                # 3. टेलीग्राम API के जरिए यूजर को वापस रिप्लाई भेजना (sendMessage)
                                send_url = f"{base_url}/sendMessage"
                                payload = {
                                    "chat_id": chat_id,
                                    "text": reply_text
                                }
                                requests.post(send_url, json=payload)
                                print(f"Sent reply to {chat_id}")
                                
            # हर 2 सेकंड में चेक करना ताकि रिस्पॉन्स फ़ास्ट मिले
            time.sleep(2)
            
        except Exception as e:
            print("Error in bot thread:", e)
            time.sleep(5)

if __name__ == "__main__":
    t = Thread(target=handle_bot_logic)
    t.daemon = True
    t.start()
    
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)

