
Steps to have a very simple RabbitMQ based Producer & Consumer Up & Running - 
 

 1. Install RabbitMG -
 
 
    a. Install ER Lang - https://www.erlang.org/downloads as Administrator.
    b. Install RabbitMQ(Testing on Windows 10, 64 bits) - https://www.rabbitmq.com/install-windows.html
    
 2. Starting/Running RabbitMQ - 


    i. Windows Start - RabbitMQ Command Prompt.

    ii. .\rabbitmq-service.bat stop

    iii. .\rabbitmq-service.bat uninstall, 

    iv. set HOMEDRIVE=C:, 

    v. .\rabbitmq-service.bat install, 

    vi. .\rabbitmq-service.bat start, 

    vii. .\rabbitmq-plugins.bat enable rabbitmq_management
    
    (https://stackoverflow.com/questions/56601031/directory-name-is-invalid-etc-with-rabbitmq-plugins-on-windows)
    
    ("WARNING: Using RABBITMQ_ADVANCED_CONFIG_FILE: C:\Users\CS00028477\AppData\Roaming\RabbitMQ\advanced.config")
    
3.  Test RabbitMQ running or not -


    http://localhost:15672/
    Default User : guest
    Default Password : guest
    
----------------------------------------------
 Installing RabbitMQ on macOS
 
    i. install Homebrew if it is not already installed -
        /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
        
    ii. install erlang - 
        brew install erlang
    
    iii. install RabbitMQ
        brew install rabbitmq
    
The RabbitMQ server scripts and CLI tools are installed in sbin directory 
under /usr/local/Cellar/rabbitmq, which is accessible via /usr/local/opt/rabbitmq/sbin. 
In case that directory is not in PATH it's recommend to append it:

    export PATH=$PATH:/usr/local/opt/rabbitmq/sbin
    
    Start RabbitMQ
        rabbitmq-server
        
    
----------------------------------------------
To running example "rabbit-mq-consumer" && "rabbit-mq-producer" we need to have a running RabbitMQ in @localhost.
   