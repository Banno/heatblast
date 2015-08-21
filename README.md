# heatblast

![](http://vignette2.wikia.nocookie.net/ben10/images/2/20/Heatblast_omniverse_official.png/revision/latest/scale-to-width-down/160?cb=20141129031156)

[http://ben10.wikia.com/wiki/Heatblast](http://ben10.wikia.com/wiki/Heatblast)

A [Mesos](http://mesos.apache.org) framework for scheduling [Samza](http://samza.apache.org) jobs. Built during the [Mesoscon 2015 Hackathon](http://mesoscon2015.sched.org/event/c210df078c68e5d78151ed7e664b3c4a?iframe=no&w=i:0;&sidebar=yes&bg=no#.VdTFK5NVhBc).

The existing Samza Mesos framework https://github.com/Banno/samza-mesos runs one Mesos framework for each Samza job. When you have a large number of Samza jobs, this creates a large number of Mesos frameworks. We've run into problems in this case where Mesos will stop sending offers to Marathon after Marathon runs all of these Samza frameworks.

This Samza Mesos framework runs a single Mesos framework for all Samza jobs, somewhat like a Marathon for Samza. It provides a REST/JSON API to submit jobs, query job status, etc and monitors execution of Samza jobs. Each Samza job runs as a set of Mesos tasks, one task per Samza container.

Managed via [waffle.io](https://waffle.io): [![Stories in Ready](https://badge.waffle.io/Banno/heatblast.svg?label=ready&title=Ready)](http://waffle.io/Banno/heatblast)

## Building

```
sbt clean compile docker
```

## Running

```
big up -d marathon kafka

cd heatblast-scheduler/marathon
./run.sh

#Wait for Heatblast scheduler to run

curl -X POST -H "Content-Type: application/json" -d '{"jobName": "ExampleSamzaTask", "dockerImage": "registry.banno-internal.com/heatblast-example-samza:latest"}' http://dev.banno.com:8181/jobs
```
