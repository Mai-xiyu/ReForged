package net.neoforged.neoforge.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ItemAttributeModifierEvent extends net.neoforged.bus.api.Event {
	private final ItemStack stack;
	private final ItemAttributeModifiers defaultModifiers;
	private ItemAttributeModifiersBuilder builder;

	@ApiStatus.Internal
	public ItemAttributeModifierEvent(ItemStack stack, ItemAttributeModifiers defaultModifiers) {
		this.stack = stack;
		this.defaultModifiers = defaultModifiers;
	}

	public ItemStack getItemStack() {
		return this.stack;
	}

	public ItemAttributeModifiers getDefaultModifiers() {
		return this.defaultModifiers;
	}

	public List<ItemAttributeModifiers.Entry> getModifiers() {
		return this.builder == null ? this.defaultModifiers.modifiers() : this.builder.getEntryView();
	}

	public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
		return getBuilder().addModifier(attribute, modifier, slot);
	}

	public boolean removeModifier(Holder<Attribute> attribute, ResourceLocation id) {
		return getBuilder().removeModifier(attribute, id);
	}

	public void replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
		getBuilder().replaceModifier(attribute, modifier, slot);
	}

	public boolean removeIf(Predicate<ItemAttributeModifiers.Entry> condition) {
		return getBuilder().removeIf(condition);
	}

	public boolean removeAllModifiersFor(Holder<Attribute> attribute) {
		return getBuilder().removeIf(entry -> entry.attribute().equals(attribute));
	}

	public void clearModifiers() {
		getBuilder().clear();
	}

	public ItemAttributeModifiers build() {
		return this.builder == null ? this.defaultModifiers : this.builder.build(this.defaultModifiers.showInTooltip());
	}

	private ItemAttributeModifiersBuilder getBuilder() {
		if (this.builder == null) {
			this.builder = new ItemAttributeModifiersBuilder(this.defaultModifiers);
		}
		return this.builder;
	}

	private static class ItemAttributeModifiersBuilder {
		private final List<ItemAttributeModifiers.Entry> entries;
		private final Map<Key, ItemAttributeModifiers.Entry> entriesByKey;

		ItemAttributeModifiersBuilder(ItemAttributeModifiers defaultModifiers) {
			this.entries = new LinkedList<>();
			this.entriesByKey = new HashMap<>(defaultModifiers.modifiers().size());

			for (ItemAttributeModifiers.Entry entry : defaultModifiers.modifiers()) {
				entries.add(entry);
				entriesByKey.put(new Key(entry.attribute(), entry.modifier().id()), entry);
			}
		}

		List<ItemAttributeModifiers.Entry> getEntryView() {
			return Collections.unmodifiableList(this.entries);
		}

		boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
			Key key = new Key(attribute, modifier.id());
			if (entriesByKey.containsKey(key)) {
				return false;
			}
			ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(attribute, modifier, slot);
			entries.add(entry);
			entriesByKey.put(key, entry);
			return true;
		}

		boolean removeModifier(Holder<Attribute> attribute, ResourceLocation id) {
			ItemAttributeModifiers.Entry entry = entriesByKey.remove(new Key(attribute, id));
			if (entry != null) {
				entries.remove(entry);
				return true;
			}
			return false;
		}

		@Nullable
		ItemAttributeModifiers.Entry replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
			Key key = new Key(attribute, modifier.id());
			ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(attribute, modifier, slot);
			if (entriesByKey.containsKey(key)) {
				ItemAttributeModifiers.Entry previousEntry = entriesByKey.get(key);
				int index = entries.indexOf(previousEntry);
				if (index != -1) {
					entries.set(index, entry);
				} else {
					entries.add(entry);
				}
				entriesByKey.put(key, entry);
				return previousEntry;
			}
			entries.add(entry);
			entriesByKey.put(key, entry);
			return null;
		}

		boolean removeIf(Predicate<ItemAttributeModifiers.Entry> condition) {
			this.entries.removeIf(condition);
			return this.entriesByKey.values().removeIf(condition);
		}

		void clear() {
			this.entries.clear();
			this.entriesByKey.clear();
		}

		ItemAttributeModifiers build(boolean showInTooltip) {
			return new ItemAttributeModifiers(ImmutableList.copyOf(this.entries), showInTooltip);
		}

		private record Key(Holder<Attribute> attr, ResourceLocation id) {
		}
	}
}
