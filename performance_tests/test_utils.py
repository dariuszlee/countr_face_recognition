import subprocess

from py4j.java_gateway import JavaGateway
from py4j.java_gateway import GatewayParameters

def main():
    """TODO: Docstring for main.
    :returns: TODO

    """
    server_proc = subprocess.Popen(['java', '-jar', "./faceserver.jar"])
    clients = 4
    
    for port in range(25335, 25335 + clients):
        gateway = JavaGateway(gateway_parameters=GatewayParameters(port=port))
        face_client = gateway.entry_point
        

        __import__('ipdb').set_trace()
        __import__('pprint').pprint(face_client)

    server_proc.kill()

if __name__ == "__main__":
    main()
