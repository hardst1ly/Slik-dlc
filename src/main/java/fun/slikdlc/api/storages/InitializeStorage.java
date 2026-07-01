package fun.slikdlc.api.storages;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.storages.implement.CommandStorage;
import fun.slikdlc.api.storages.implement.ConfigStorage;
import fun.slikdlc.api.storages.implement.FreeLookStorage;
import fun.slikdlc.api.storages.implement.FriendStorage;
import fun.slikdlc.api.storages.implement.LocalizationStorage;
import fun.slikdlc.api.storages.implement.MacroStorage;
import fun.slikdlc.api.storages.implement.ModuleStorage;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.storages.implement.StaffStorage;
import fun.slikdlc.api.storages.implement.ThemeStorage;
import fun.slikdlc.api.storages.implement.WaypointStorage;
import fun.slikdlc.api.utils.tps.TPSCalc;

public class InitializeStorage implements QClient {
   public InitializeStorage() {
   }

   public void onInitialize() {
      EventInvoker.register(this);
      this.initStorages();
   }

   public void initStorages() {
      SlikDlc.INSTANCE.moduleStorage = new ModuleStorage();
      SlikDlc.INSTANCE.themeStorage = new ThemeStorage();
      SlikDlc.INSTANCE.tpsCalc = new TPSCalc();
      EventInvoker.register(SlikDlc.INSTANCE.tpsCalc);
      SlikDlc.INSTANCE.localizationStorage = new LocalizationStorage();
      SlikDlc.INSTANCE.freeLookStorage = new FreeLookStorage();
      SlikDlc.INSTANCE.rotationStorage = new RotationStorage();
      SlikDlc.INSTANCE.friendStorage = new FriendStorage();
      SlikDlc.INSTANCE.macroStorage = new MacroStorage();
      SlikDlc.INSTANCE.staffStorage = new StaffStorage();
      SlikDlc.INSTANCE.waypointStorage = new WaypointStorage();
      SlikDlc.INSTANCE.commandStorage = new CommandStorage();
      SlikDlc.INSTANCE.configStorage = new ConfigStorage();
   }
}
