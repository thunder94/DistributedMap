# DistributedMap
Share state of a Map between application instances

## About
Each application instance (so called node) has its own Map. Now, when we want to ```get``` something from the Map or check if it
```contains``` some key, we can do it on our local copy. But when we want to ```put``` or ```remove``` item, we need to notify
other instances, so they can update their own Maps. Here JGroups library comes with help. Notifications can be done in form of 
sending messages. In order to do that, all applications have to connect to the same channel. When a new node connects, it has to
get current state of the Map from the group. It is done thanks to ```getState()``` and ```setState()``` methods. Connection
problems and potential partitions are handled by ```viewHandler``` class and ```viewAccepted()``` method. For more info check
JGroups documentation: http://www.jgroups.org/manual/index.html#HandlingNetworkPartitions
For message serializing Google's Protobuf was used

## Install and run
### Prerequisites
```
JGropus 3.6.13.Final
Google Protobuf 2.4.1
```
Install manually or use *Maven* instead

### Running
Simply run App class

### Map operations

To get element associated with certain key, type:
```
get key
```
To check if Map contains element:
```
contains key
```
To put {key, value} into Map:
```
put key value
```
To remove:
```
remove key
```

Now launch multiple instances and see if they share the Map state!
