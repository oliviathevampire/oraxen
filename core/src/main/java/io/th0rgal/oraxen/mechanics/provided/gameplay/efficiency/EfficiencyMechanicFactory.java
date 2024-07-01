package io.th0rgal.oraxen.mechanics.provided.gameplay.efficiency;

import com.comphenix.protocol.ProtocolLibrary;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.utils.PluginUtils;
import io.th0rgal.oraxen.utils.VersionUtil;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;

public class EfficiencyMechanicFactory extends MechanicFactory {

    public EfficiencyMechanicFactory(ConfigurationSection section) {
        super(section);
        if (PluginUtils.isEnabled("ProtocolLib")) {
            ProtocolLibrary.getProtocolManager().getPacketListeners().stream().filter(l -> l.getClass().equals(EfficiencyMechanicListener.class))
                            .findFirst().ifPresent(l -> ProtocolLibrary.getProtocolManager().removePacketListener(l));
            ProtocolLibrary.getProtocolManager().addPacketListener(new EfficiencyMechanicListener(this));
        }
    }

    @Override
    public Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        Mechanic mechanic = new EfficiencyMechanic(this, itemMechanicConfiguration);
        if (VersionUtil.atOrAbove("1.20.5")) {
            Logs.logWarning(mechanic.getItemID() + " is using deprecated Efficiency-Mechanic...");
            Logs.logWarning("It is heavily advised to swap to the new `tool`-property on 1.20.5+ servers...");
        }
        addToImplemented(mechanic);
        return mechanic;
    }

    public EfficiencyMechanicFactory getInstance() {
        return this;
    }

}
