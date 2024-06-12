# Sys Screenuploader Relay

Automatically post your nintendo switch screenshots and videos to discord, without hosting your data on an different hosting provider.

This is an alternative backend server for the popular switch sysmodule [Sys-Screenuploader](https://github.com/bakatrouble/sys-screenuploader) 

## Self-Hosting
Currently you need to compile it yourself. Then you can run the "-all.jar".

Right now there is no configuration file, the port is hardcoded to `9000`.

## Usage
- Open your self hosted instance at `<ip:9000>` OR use the instance provided my myself: https://api.erdbeerbaerlp.de/sysscreenuploader/
- Enter an valid discord webhook url and use the converted url in your sys-screenuploader configuration file
