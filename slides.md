---
title: CRaCing Java Snapshots
author: Pasha Finkelshteyn
theme:
  name: catppuccin-macchiato
---

# `whoami`?

- Geek
<!-- pause -->
- Developer 🥑 at Bellsoft (we'll talk about it later)
<!-- pause -->
- JVM developer: Java and Kotlin (and actually Clojure, Scala, Ceylon 😱)
<!-- pause -->
- DevOps is not a person, but I could be!
<!-- pause -->
- Long-time Linux User (Did I say I'm a geek?)
<!-- pause -->
- If life gives me a terminal 🍋, I have to make a presentation 🥛


<!-- end_slide -->

# BellSoft

https://bell-sw.com

- Created in 2017 to support ARM32 builds of OpenJDK
- Contributes to OpenJDK (and GraalVM)
- Has 2 main products: Liberica JDK and Alpaquita Linux
- Has a flavour of Liberica with CRaC support
- Major clients include Visa, RSA, Broadcom
- Liberica is officially recommended by Spring

<!-- end_slide -->

# Why do we need snapshots?

<!-- pause -->

Because Java is slow ;)

<!-- pause -->

Just kidding.
<!-- pause -->
But applications actually are!

<!-- pause -->

```plain
Started PetClinicApplication in 4.932 HOURS
```
<!-- alignment: center -->
↑ That's how muggles see it
<!-- alignment: left -->

<!-- end_slide -->

# Why do we need snapshots?

```bash +exec +acquire_terminal
java -jar /home/finkel/work_self/spring-petclinic/build/libs/spring-petclinic-3.4.0.jar
```

<!-- pause -->

What was it? 5 seconds or so?

<!-- end_slide -->
<!-- alignment: center -->
# How do we tackle it?
<!-- end_slide -->

# CRIU

https://criu.org/Main_Page

> Checkpoint/Restore In Userspace, or **CRIU** (pronounced kree-oo, IPA: /krɪʊ/, Russian: криу), is a Linux software. It can freeze a running container (or an individual application) and checkpoint its state to disk. The data saved can be used to restore the application and run it exactly as it was during the time of the freeze

<!-- end_slide -->
# WOW, that's amazing!

Example:

```bash
for i in $(seq 1 10000); do
  echo $i
  sleep 1
done
```

<!-- pause -->

Run it: `./my.sh`

<!-- pause -->

Wait some time

<!-- pause -->

CRIU it: `criu --shell-job dump -t $(pidof -x ./my.sh)`

<!-- pause -->

Restore it: `criu --shell-job restore`

<!-- end_slide -->
<!-- alignment: center -->
# Demo
<!-- end_slide -->

# It's perfect! 😍

<!-- pause -->

## But

It doesn't _always_ work
<!-- pause -->

This is how it fails:

```bash +exec +acquire_terminal
sudo criu dump -t $(pidof -x ghostty)
```
<!-- end_slide -->
# And _of course_ it won't work for Java 😥

<!-- pause -->

But this talk wouldn't be a thing, right?

<!-- end_slide -->
# CRaC

https://openjdk.org/projects/crac/

> The CRaC (Coordinated Restore at Checkpoint) Project researches coordination of Java programs with mechanisms to checkpoint (make an image of, snapshot) a Java instance while it is executing. Restoring from the image could be a solution to some of the problems with the start-up and warm-up times. The primary aim of the Project is to develop a new standard mechanism-agnostic API to notify Java programs about the checkpoint and restore events

<!-- end_slide -->

# CRaC

CRaC is not a canonical part of JDK.

Some JDKs include it, for example Liberica JDK

<!-- pause -->

*Advertising space for rent:*

https://bell-sw.com/pages/downloads/#jdk-21-lts

<!-- pause -->

_CRaC is first implemented by Azul, we support its fork_

<!-- end_slide -->

# What I do and what I do not

- I say CRaC is nice
- I do not say there are no other solutions
- I say developer experience and productivity are good with CRaC
- I do not hide downsides of CRaC, but please ask me if I forgot something

<!-- end_slide -->

# CRaC

Not even all builds support CRaC (it makes the distribution larger)

<!-- end_slide -->

# CRaC example

```java {1-4,8-9|4,5,9|4-6,9|5-8}
public static void main(String args[]) throws InterruptedException {
  // This is a part of the saved state
  long startTime = System.currentTimeMillis();
  for(int counter: IntStream.range(1, 10000).toArray()) {
    Thread.sleep(1000);
    long currentTime = System.currentTimeMillis();
    System.out.println("Counter: " + counter + "(passed " + (currentTime-startTime) + " ms)");
    startTime = currentTime;
  }
}
```

<!-- end_slide -->

# CRaC Example

```docker
FROM bellsoft/liberica-runtime-container:jdk-crac-slim

ADD Example.java /app/Example.java
WORKDIR /app
RUN javac Example.java
ENTRYPOINT java -XX:CRaCCheckpointTo=/app/checkpoint Example
```

<!-- pause -->

```bash +exec
docker build -t pre_crack -f crac2/Dockerfile crac2
```
<!-- end_slide -->
# Now let's crack it and launch from snapshot!

```bash
docker run --cap-add CAP_SYS_PTRACE --cap-add CAP_CHECKPOINT_RESTORE -d pre_crack
```

<!-- pause -->

- `CAP_SYS_PTRACE`: we need to access the whole process tree
  - transfer data to or from the memory of arbitrary processes using process_vm_readv(2) and process_vm_writev(2)
<!-- pause-->
- `CAP_CHECKPOINT_RESTORE`: somehow there is a special cap for this
  - Update /proc/sys/kernel/ns_last_pid; Read the contents of the symbolic links in /proc/pid/map_files for other processes

<!-- pause -->

**Do not add `--rm`, we'll need this image later**

<!-- end_slide -->

# Now let's crack it and launch from snapshot!

```shell +exec
ID=$(docker run --cap-add CAP_SYS_PTRACE --cap-add CAP_CHECKPOINT_RESTORE -d pre_crack)
sleep 5
docker exec -it $ID jcmd 129 JDK.checkpoint
docker commit $ID post_crack
echo Checkpointed!
sleep 5
```

<!-- pause -->

Let's look into our creation

```shell +exec +acquire_terminal
dive post_crack
```

<!-- pause -->
And run it!

```shell +exec
docker run --rm --entrypoint java post_crack -XX:CRaCRestoreFrom=/app/checkpoint
```

<!-- end_slide -->

# CRaCing Spring

Spring Boot supports CRaC out of the box!

(version 3.2+)

<!-- pause -->

Demo!
<!-- end_slide -->

```shell +exec +acquire_terminal
PET=/home/finkel/work_self/spring-petclinic
pushd $PET
./gradlew build -xtest
popd
cp $PET/build/libs/spring-petclinic-3.4.0.jar crac-spring
```
<!-- pause -->

```bash +exec +acquire_terminal
docker build -t pre_crack -f crac-spring/Dockerfile crac-spring
sleep 3
```

<!--pause -->
You know the drill...

<!-- end_slide -->

# Is everything that easy?

<!-- pause -->

You guessed id!
<!-- pause -->
Let's break it!

<!-- end_slide -->

# Now you know the basics!

1. CRaC allows to startup any software almost instantly
2. Building Docker with CRaC is not simple but doable
   1. Build jar
   2. Build docker with this jar startup in entrypoint
   3. Run image with `--privileged` or with `--cap-add`
   4. Checkpoint it
   5. Commit
3. To support custom things in application we should
   1. Implement `Resource`
   2. Register it in `Core`

<!-- end_slide -->

# Thank you! Questions?

- Site    : https://asm0dey.site
- Bluesky : @asm0dey.site
- Twitter : @asm0di0
- Mastodon: @asm0dey@fosstodon.org
- LinkedIn: @asm0dey
- E-mail  : me@asm0dey.site
