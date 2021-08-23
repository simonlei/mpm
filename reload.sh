docker pull ghcr.io/simonlei/mpm:$1 && \
        docker stop mpm && \
        docker rm mpm && \
        docker run  --net=host --name mpm -v /home/simon/config:/config -v /home/simon/logs:/logs -d ghcr.io/simonlei/mpm:$1