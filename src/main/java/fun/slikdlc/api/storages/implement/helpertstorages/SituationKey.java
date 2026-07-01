package fun.slikdlc.api.storages.implement.helpertstorages;

import lombok.Generated;

public final class SituationKey {
   private final String targetType;
   private final String distanceBucket;
   private final String movementState;
   private final String critState;
   private final String healthState;

   @Override
   public String toString() {
      return this.targetType + "_" + this.distanceBucket + "_" + this.movementState + "_" + this.critState + "_" + this.healthState;
   }

   @Generated
   public SituationKey(String targetType, String distanceBucket, String movementState, String critState, String healthState) {
      this.targetType = targetType;
      this.distanceBucket = distanceBucket;
      this.movementState = movementState;
      this.critState = critState;
      this.healthState = healthState;
   }

   @Generated
   public String getTargetType() {
      return this.targetType;
   }

   @Generated
   public String getDistanceBucket() {
      return this.distanceBucket;
   }

   @Generated
   public String getMovementState() {
      return this.movementState;
   }

   @Generated
   public String getCritState() {
      return this.critState;
   }

   @Generated
   public String getHealthState() {
      return this.healthState;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SituationKey other)) {
         return false;
      } else {
         Object this$targetType = this.getTargetType();
         Object other$targetType = other.getTargetType();
         if (this$targetType == null ? other$targetType == null : this$targetType.equals(other$targetType)) {
            Object this$distanceBucket = this.getDistanceBucket();
            Object other$distanceBucket = other.getDistanceBucket();
            if (this$distanceBucket == null ? other$distanceBucket == null : this$distanceBucket.equals(other$distanceBucket)) {
               Object this$movementState = this.getMovementState();
               Object other$movementState = other.getMovementState();
               if (this$movementState == null ? other$movementState == null : this$movementState.equals(other$movementState)) {
                  Object this$critState = this.getCritState();
                  Object other$critState = other.getCritState();
                  if (this$critState == null ? other$critState == null : this$critState.equals(other$critState)) {
                     Object this$healthState = this.getHealthState();
                     Object other$healthState = other.getHealthState();
                     return this$healthState == null ? other$healthState == null : this$healthState.equals(other$healthState);
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $targetType = this.getTargetType();
      result = result * 59 + ($targetType == null ? 43 : $targetType.hashCode());
      Object $distanceBucket = this.getDistanceBucket();
      result = result * 59 + ($distanceBucket == null ? 43 : $distanceBucket.hashCode());
      Object $movementState = this.getMovementState();
      result = result * 59 + ($movementState == null ? 43 : $movementState.hashCode());
      Object $critState = this.getCritState();
      result = result * 59 + ($critState == null ? 43 : $critState.hashCode());
      Object $healthState = this.getHealthState();
      return result * 59 + ($healthState == null ? 43 : $healthState.hashCode());
   }
}
