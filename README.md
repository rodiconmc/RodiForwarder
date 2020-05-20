# RodiForwarder

This allows you to map one port to multiple minecraft servers via domains

## Usage

Find the latest docker image at https://github.com/rodiconmc/RodiForwarder/packages

Set the environment variable LISTENPORT to the port, usually 25565

Set the environment variable RODIFORWARD like this:
`[DOMAIN1];[HOST1:PORT1] [DOMAIN2];[HOST2:PORT2] [DOMAIN3];[HOST3:PORT3]` etc...

For example, the environment variable

`RODIFORWARD="alpha.rodiconmc.com;10.0.0.1:25565 beta.rodiconmc.com;10.0.0.2:25565"`

Would mean that users connecting to `alpha.rodiconmc.com` would be forwarded to `10.0.0.1:25565`, and users connecting to `beta.rodiconmc.com` would be forwarded to `10.0.0.2:25565`
