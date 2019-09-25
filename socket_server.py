import socket
import time
import pickle
import sys
import traceback
from threading import Thread

def main():
    start_server()


def start_server():
    host = "127.0.0.1"
    port = 8100 # arbitrary non-privileged port
    soc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    soc.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        soc.bind((host, port))
    except:
        print("Bind failed. Error : " + str(sys.exc_info()))
        sys.exit()
    soc.listen(6) # queue up to 6 requests
    print("Socket now listening")
    # infinite loop- do not reset for every requests
    while True:
        connection, address = soc.accept()
        ip, port = str(address[0]), str(address[1])
        print("Connected with " + ip + ":" + port)
        try:
            Thread(target=client_thread, args=(connection, ip, port)).start()
        except:
            print("Thread did not start.")
            traceback.print_exc()
    soc.close()


def client_thread(connection, ip, port, max_buffer_size = 921764):
    is_active = True
    count = 0
    start_time = time.time()
    while is_active:
        client_input = receive_input(connection, max_buffer_size)
        if "--QUIT--" in client_input:
            print("Client is requesting to quit")
            connection.close()
            print("Connection " + ip + ":" + port + " closed")
            is_active = False
        else:
            # print("Processed result: {}".format(client_input))
            connection.sendall("-".encode("utf8"))
            count += 1
            if count % 100 == 0:
                now = time.time()
                print(count, " ", now - start_time)
                start_time = now


def receive_input(connection, max_buffer_size, left_over = b""):
    client_input = b""
    while True:
        client_input_cur = connection.recv(max_buffer_size)
        client_input += client_input_cur
        if len(client_input) >= 921764:
            break
    raw_frame, left_over = client_input[0:921764], client_input[921764:]

    # client_input_size = sys.getsizeof(client_input)
    # if client_input_size > max_buffer_size:
    #     print("The input size is greater than expected {}".format(client_input_size))
    real_frame = pickle.loads(raw_frame)
    # decoded_input = client_input.decode("utf8").rstrip()
    return real_frame, left_over


# def process_input(input_str):
#    print("Processing the input received from client")
#    return "Hello " + str(input_str).upper()


if __name__ == "__main__":
   main()
