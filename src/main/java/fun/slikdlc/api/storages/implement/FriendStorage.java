package fun.slikdlc.api.storages.implement;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class FriendStorage {
   private final List<String> friends = new ArrayList<>();

   public FriendStorage() {
   }

   public void add(String friend) {
      if (!friend.isEmpty()) {
         this.friends.add(friend);
      }
   }

   public void remove(String friend) {
      this.friends.remove(friend);
   }

   public void clear() {
      this.friends.clear();
   }

   public boolean isFriend(String friend) {
      return this.friends.contains(friend);
   }

   public boolean isEmpty() {
      return this.friends.isEmpty();
   }

   @Generated
   public List<String> getFriends() {
      return this.friends;
   }
}
