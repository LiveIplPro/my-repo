import os
import time
import requests
from threading import Thread
from flask import Flask

# 1. यह हिस्सा क्लाउड सर्वर को जिंदा रखने के लिए है (वेब सर्वर)
app = Flask(__name__)

@app.route('/')
def home():
    return "Mera Server 24/7 Chalu Hai!", 200

# 2. यह हिस्सा आपका असली काम 24/7 बैकग्राउंड में करेगा
def mera_background_task():
    # अपना असली टेलीग्राम बॉट टोकन यहाँ डालें
    bot_token = "8474712057:AAHAOpGfcm5fdoiwgyF9dYQyzaCH4_46J4g"
    
    while True:
        try:
            # यहाँ वह काम लिखें जो आप बार-बार करवाना चाहते हैं
            # उदाहरण: टेलीग्राम बॉट पर नए मैसेज चेक करना
            url = f"https://telegram.org{bot_token}/getUpdates"
            response = requests.get(url, timeout=10)
            
            print("Server check kar raha hai...", response.status_code)
            
            # हर 10 सेकंड के बाद यह काम दोबारा होगा
            time.sleep(10)
            
        except Exception as e:
            print("Koi galti hui:", e)
            time.sleep(15)

# 3. यह कोड को स्टार्ट करने का मुख्य पॉइंट है
if __name__ == "__main__":
    # बैकग्राउंड काम को एक अलग रास्ते (Thread) पर शुरू करना
    t = Thread(target=mera_background_task)
    t.daemon = True
    t.start()
    
    # सर्वर को चालू करना (पोर्ट नंबर क्लाउड खुद तय करेगा)
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)
