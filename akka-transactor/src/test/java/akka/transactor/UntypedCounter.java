/**
 * Copyright (C) 2009-2011 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.transactor;

import akka.actor.ActorRef;
import akka.transactor.UntypedTransactor;
import akka.transactor.SendTo;
import static scala.concurrent.stm.JavaAPI.*;
import scala.concurrent.stm.Ref;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UntypedCounter extends UntypedTransactor {
    private String name;
    private Ref.View<Integer> count = newRef(0);

    public UntypedCounter(String name) {
        this.name = name;
    }

    @Override public Set<SendTo> coordinate(Object message) {
        if (message instanceof Increment) {
            Increment increment = (Increment) message;
            List<ActorRef> friends = increment.getFriends();
            if (!friends.isEmpty()) {
                Increment coordMessage = new Increment(friends.subList(1, friends.size()), increment.getLatch());
                return include(friends.get(0), coordMessage);
            } else {
                return nobody();
            }
        } else {
            return nobody();
        }
    }

    public void atomically(Object message) {
        if (message instanceof Increment) {
            increment(count, 1);
            final Increment increment = (Increment) message;
            Runnable countDown = new Runnable() {
                public void run() {
                    increment.getLatch().countDown();
                }
            };
            afterRollback(countDown);
            afterCommit(countDown);
        }
    }

    @Override public boolean normally(Object message) {
        if ("GetCount".equals(message)) {
            getSender().tell(count.get());
            return true;
        } else return false;
    }
}
