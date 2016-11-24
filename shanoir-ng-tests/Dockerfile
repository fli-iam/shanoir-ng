FROM ubuntu
RUN apt-get update && apt-get install -y firefox xvfb python-pip && pip install selenium && \
mkdir -p /root/tests && mkdir -p /root/bin && rm -rf /var/lib/apt/lists/* 

ENV PATH $PATH:/root/bin
ENV DISPLAY=:99

COPY tests /root/tests

ADD geckodriver-v0.11.1-linux64.tar.gz /root/bin
RUN cd /root/bin && chmod +x geckodriver && cp geckodriver wires

CMD /root/tests/test_launch.sh