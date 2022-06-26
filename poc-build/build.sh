# 1. buildfiles pull
while :
do
    if [ ! -d ./So1s-PoC-BuildFiles ];
    then
        echo "git cloning..."
        git clone https://github.com/shinilseop/So1s-PoC-BuildFiles.git
    else
        echo "git cloning is finish."
        break
    fi
    sleep 1
done

# 2. install python module
while :
do
    search=`pip list | grep -i bentoml`
    if [ -z ${search} ];
    then
        echo "python module installing..."
        pip3 install -r ./So1s-PoC-BuildFiles/requirements.txt
    else
        echo "python module install finished!"
        break
    fi
    sleep 1
done

# 3. set protobuf env value
echo "export PROTOCOL_BUFFERS_PYTHON_IMPLEMENTATION=python"
export PROTOCOL_BUFFERS_PYTHON_IMPLEMENTATION=python

# 4.bentoml bundling
while :
do
    if [ ! -d /root/bentoml/repository ]; 
    then
        echo "bentoml bundling..."
        python3.8 ./So1s-PoC-BuildFiles/src/main.py
    else
        echo "bentoml bundle is created."
        break
    fi
    sleep 1
done

# 5. bentoml containerizing
bentoml containerize TransformerService:latest -t poc-infer:0.1