from flask import Flask, Response, request, render_template, jsonify
import numpy as np
import json
import cv2
import mediapipe as mp
from tensorflow.keras.models import load_model
import tensorflow as tf
import mysql.connector
from mysql.connector import Error
from datetime import datetime
import time

user_id = None

conn = mysql.connector.connect(host="localhost",
                                    user="root",
                                    password="",
                                    database="flask")

mp_drawing = mp.solutions.drawing_utils
mp_pose = mp.solutions.pose
mp_holistic = mp.solutions.holistic

model = load_model("mobilenet.h5")

posture = ['correct',
 'head_down',
 'head_left',
 'head_right',
 'leaning_left',
 'leaning_right',
 'left_hand_up',
 'right_hand_up'
]

wrong_postures = set(posture) - {'correct'}

user_status = {'last_wrong_time': None, 'last_status': 'correct', 'posture_detected': 'correct'}

app = Flask(__name__)

video_data = b''

def pose_estimation(frame):
    frameWidth = frame.shape[1]
    frameHeight = frame.shape[0]
   
 
    with mp_pose.Pose(
            static_image_mode=True,
            model_complexity=1,
            min_detection_confidence=0.5) as pose:
            # Chuyển đổi ảnh từ BGR sang RGB trước khi xử lý.
            results = pose.process(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
            # remove background
            
              # Tạo ảnh nền đen
            black_image = np.zeros_like(frame)
            # Vẽ đường nối các khung xương trên ảnh nền đen
            mp_drawing.draw_landmarks(black_image, results.pose_landmarks, mp_pose.POSE_CONNECTIONS)
            
            annotated_image = frame.copy()
            mp_drawing.draw_landmarks(annotated_image, results.pose_landmarks, mp_pose.POSE_CONNECTIONS)
 
    return annotated_image, black_image


def generate_frames():
    try:
        global video_data
        while True:
            if video_data:
                nparr = np.frombuffer(video_data, np.uint8)
                frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
                # imgNp = np.array(bytearray(video_data.read()), dtype=np.uint8)
                # frame = cv2.imdecode(imgNp, -1)
                frame = cv2.resize(frame, (320, 240))
                # print(frame.shape)
                frame, img = pose_estimation(frame)
    
                # img = pose_estimation(frame, black_bg=True)
                img = tf.image.resize(img, (224,224))
                result = model.predict(np.expand_dims(img/255, 0))

                predicted_posture = posture[np.argmax(result)]
                current_time = time.time()

                if predicted_posture in wrong_postures:
                    if user_status['last_status'] == 'correct':
                        user_status['last_wrong_time'] = current_time
                        user_status['posture_detected'] = predicted_posture
                    elif current_time - user_status['last_wrong_time'] >= 10:
                        user_status['last_status'] = 'wrong'
                        user_status['posture_detected'] = predicted_posture
                    user_status['last_status'] = 'wrong'
                else:
                    user_status['last_status'] = 'correct'
                    user_status['last_wrong_time'] = None

                cv2.putText(frame, posture[np.argmax(result)], (10, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0))

                # current_date_time = datetime.now()
                # hour_value = current_date_time.strftime('%H:%M:%S')
                # day_value = current_date_time.strftime('%Y-%m-%d')
                # cursor = conn.cursor()
                # custom_field = posture[np.argmax(result)]
                # sql = "INSERT INTO `posture_data` (hour, day, {}, user_id) VALUES (%s, %s, TRUE, %s)".format(custom_field)
                # val = (hour_value, day_value, 7)
                # cursor.execute(sql, val)
                # conn.commit()
            
                ret, buffer = cv2.imencode('.jpg', frame)
                yield (b'--frame\r\n'
                    b'Content-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')
    except Exception as e:
        return "failed"


#lấy dữ liệu từ esp32
@app.route('/upload', methods=['POST'])
def upload():
    global video_data
    video_data = request.data
    return "OK"

@app.route('/video_feed')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')


@app.route('/')
def index():
    return render_template('index.html')

@app.route('/sign_up', methods=['POST'])
def sign_up():
    try:
        data = request.get_json()

        name = data['name']
        email = data['email']
        username = data['username']
        password = data['password']

        if conn.is_connected():
            cursor = conn.cursor()

            sql = "INSERT INTO `user` (email, username, pass, name) VALUES (%s, %s, %s, %s)"
            val = (email, username, password, name)
            cursor.execute(sql, val)
            conn.commit()
            
            return "success"
        return "can connect to database"
    except Exception as e:
        return "failed"

@app.route('/login', methods = ['POST'])
def login():
    global user_id
    try:
        # lấy name và pass từ android để kiểm tra
        data = request.get_json()
        # print (data)
        username =  data['nameValuePairs']['username']
        password =  data['nameValuePairs']['password']

        if conn.is_connected():
            cursor = conn.cursor()

            sql = "SELECT * FROM user WHERE username = %s AND pass = %s"
            val = (username, password)
            cursor.execute(sql, val)
            user = cursor.fetchone()
    
            # nếu tìm thấy user
            if user:
                # lưu biến user_id
                user_id = user[0]

                # trả 2 biến này về để android lưu qua các file adnroid khác
                response_data = {
                    "status": "success",
                    "name": user[4], # name là cột 5 trong sql
                    "email": user[1]
                }
                
                return json.dumps(response_data) 
            else:
                return "wrong password"
        return "can connect to database"
    except Exception as e:
        return "failed"
    

@app.route('/statistic', methods = ['POST'])
def statistic():
    # lấy ngày tháng năm từ app 
    data = request.get_json()
    # print (data)
    day =  data['nameValuePairs']['day']
    month =  data['nameValuePairs']['month']
    year =  data['nameValuePairs']['year']

    new_date = "{}-{:02d}-{:02d}".format(year, month, day)

    results_dict = {}

    # đếm từng tư thế
    for i in posture:
        cursor = conn.cursor()

        sql = "SELECT COUNT(*) FROM posture_data WHERE day = %s AND user_id = %s AND {} = TRUE".format(i)

        val = (new_date, 7)  # Giả sử user_id là 7
        
        cursor.execute(sql, val)
        result = cursor.fetchone()
        results_dict[i] = result[0]

    return json.dumps(results_dict) 

@app.route('/change_pass', methods = ['POST'])
def change_pass():
    try:
        data = request.get_json()
        old_password = data['old_pass']
        new_password = data['new_pass']

        cursor = conn.cursor()
        cursor.execute("SELECT * FROM user WHERE user_id = %s AND pass = %s", (user_id, old_password))
        user = cursor.fetchone()

        if user:
            cursor.execute("UPDATE user SET pass = %s WHERE user_id = %s", (new_password, user_id))
            conn.commit()
            return "success"
        else:
            return "invalid_old_password"

    except Exception as e:
        print(e)
        return "failed"
    
# kiểm tra để phát ra loa
@app.route('/check_posture', methods=['GET'])
def check_posture():
    global user_status
    current_time = time.time()
    status = 'correct'
    posture_detected = 'correct'

    if user_status['last_status'] == 'wrong' and current_time - user_status['last_wrong_time'] >= 10:
        status = 'wrong'
        posture_detected = user_status['posture_detected']
    else:
        # Kiểm tra xem tư thế có sai không
        # Nếu sai, tăng biến đếm
        if posture_detected in wrong_postures:
            user_status['consecutive_wrong_frames'] += 1
        else:
            # Nếu đúng, đặt lại biến đếm
            user_status['consecutive_wrong_frames'] = 0

        # Kiểm tra xem có đủ số frame sai liên tiếp trong 10 giây không
        if user_status['consecutive_wrong_frames'] >= 10:
            status = 'wrong'
            posture_detected = 'wrong'

    return jsonify({'status': status, 'posture': posture_detected})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
