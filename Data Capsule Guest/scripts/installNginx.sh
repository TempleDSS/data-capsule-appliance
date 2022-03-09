apt-get install -y  nginx

ufw allow 'Nginx HTTP'

cp -rTf $USER_HOME_DIR/uploads/nginxConf /etc/nginx/conf.d
cp -rTf $USER_HOME_DIR/uploads/nginxSSL /etc/nginx/ssl

systemctl restart nginx
