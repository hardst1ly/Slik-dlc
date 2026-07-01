package fun.slikdlc.api.storages.implement;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class StaffStorage {
   private final List<String> staffs = new ArrayList<>();

   public StaffStorage() {
   }

   public void add(String friend) {
      if (!friend.isEmpty()) {
         this.staffs.add(friend);
      }
   }

   public void remove(String friend) {
      this.staffs.remove(friend);
   }

   public void clear() {
      this.staffs.clear();
   }

   public boolean isStaff(String friend) {
      return this.staffs.contains(friend);
   }

   public boolean isEmpty() {
      return this.staffs.isEmpty();
   }

   @Generated
   public List<String> getStaffs() {
      return this.staffs;
   }
}
