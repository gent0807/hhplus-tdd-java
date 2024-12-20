# 동시성 제어 방식에 대한 분석 밎 보고서

## multi-process 
* 프로세스 : 각 프로세스마다 메모리의 데이터 영역, 힙 영역, 스택 영역이 할당
* 각 프로세스가 자식 프로세스를 생성하면, multi-process를 통한 동시 작업이 가능하나 
* 시분할의 비효율성, cpu core의 context-switching으로 인한 자원과 시간 소요, 메모리 공간 차지 등의 문제 발생

## multi-thread 
* 스레드 : 각 스레드는 스택 영역만 새로 할당, 데이터 영역과 힙 영역의 데이터는 공유 가능
* cpu core의 context-switching이 발생하지 않으며, 소속된 프로세스의 기존 시간을 나눠 할당받기 때문에 비효율성 개선
* multi-process의 단점을 보완한 multi-thread 방식이 동시 작업 수행에 적합

## multi-thread 사용 시 주의사항: 동기화, 동시성 제어 
* 데이터 경쟁: 공유되는 자원에 동시 접근(read, write) 시 발생하는 문제<br>
  => 동기화 필요, lock, mutex를 사용하여 동기화, thread-safe한 데이터 구조 사용 필요

* 데드락: 두 개 이상의 스레드가 자원을 기다리며 무한 대기
  => 타임 아웃 설정
* 락 경합: 여러 스레드가 lock을 얻으려 경쟁, 성능 저하
  => 읽기-쓰기 lock 사용

## 자바 동시성 제어 
자바도 위와 같은 이유들로 multi-thread를 이용하여 동시 작업을 수행할 수 있다.<br>

자바 스레드를 동기화할 수 있는 대표적인 방식엔 Syncronized와 ReetrantLock이 있다.

1. Synchronized<br>
Synchronized 키워드를 이용하여 자바 메소드 안의 특정 코드 영역을 임계 영역으로 지정할 수 있다. <br>
임계 영역으로 지정된 코드 영역은 오직 하나의 스레드만이 접근가능 하여 동기화를 가능케 한다.
<br>
Synchronized의 경우, 임계 구역에 도달한 thread가 해당 lock release되기 전까지 무한히 대기하는 특성으로 인해
성능 저하의 원인이 될 수 있다.

* 인스턴스 메소드 동기화: 
```java
private synchronized void incrementCounter() {
		counter++;
}
```
* static 메소드 동기화: 
```java
private static synchronized void incrementCounter() {
		counter++;
}
```
* 인스턴스 메소드 안의 동기화 블록: 
```java
private void incrementCounter() {
		System.out.println("gfg");
		synchronized (this) {
			counter++;
		}
	}
```
* static 메소드 안의 동기화 블록: 

```java
private static void incrementCounter() {
  System.out.println("gfg");
  synchronized (this) {
    counter++;
  }
}
```

3. ReentrantLock

* ReentrantLock 또한 sysnchronized와 마찬가지로 임계 구역을 설정함으로써 스레드의 동기화를 가능케 한다.
* 하지만 synchronized와 다른 점은 ReetrantLock을 이용한 임계 구역 설정 시, 임계 구역에 도달한 thread가 해당 lock release되기 전<br>
까지 기다리지 않고, 무시하고 지나가게 하는 등 성능 저하를 낮추고, 순서를 보장할 수 있다는 점이다.
* ReentrantLock 객체를 생성하여 원하는 영역을 산정하여 lock 메소드와 unLock 메소드를 호출하여 임계 구역을 설정한다.
  ```java
   private final ReentrantLock lock = new ReentrantLock();
  ```

## 과제 적용

```java
 public UserPoint addPoint(long userId, long amount) {

            ReentrantLock lock  = lockMap.computeIfAbsent(userId, key -> new ReentrantLock());

            if(amount > MAX_POINT) {
                throw new IllegalArgumentException();
            }

            if(amount < 1) {
                throw new IllegalArgumentException();
            }

            lock.lock();

            try {

                long point = userPointTable.selectById(userId).point();

                if(point + amount > MAX_POINT) {
                    throw new IllegalArgumentException();
                }



                UserPoint userPoint = userPointTable.insertOrUpdate(userId, point + amount);

                pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, userPoint.updateMillis());

                return userPoint;


            }finally {
                lock.unlock();
            }




    }
```

```java
public UserPoint usePoint(long userId, long amount) {

            ReentrantLock lock = lockMap.computeIfAbsent(userId, key -> new ReentrantLock());


            if (amount > MAX_POINT) {
                throw new IllegalArgumentException();
            }

            if (amount < 1) {
                throw new IllegalArgumentException();
            }


            lock.lock();

            try {

                long point = userPointTable.selectById(userId).point();

                if (point - amount < 0) {
                    throw new IllegalArgumentException();
                }

                UserPoint userPoint = userPointTable.insertOrUpdate(userId, point - amount);

                pointHistoryTable.insert(userId, amount, TransactionType.USE, userPoint.updateMillis());

                return userPoint;
            }finally {
                lock.unlock();
            }
    }

```

* 임계 구역을 userPointTable, pointHistoryTable과 같이 공유되는 자원을 다루는 부분으로 산정하였다.

* 단순 synchronized 키워드를 메소드에 사용할 경우, 두 메소드로 나뉘어져 있는 임계 구역이 같은 lock울 공유하기 때문에 



