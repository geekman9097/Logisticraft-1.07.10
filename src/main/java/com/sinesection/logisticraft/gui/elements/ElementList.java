package com.sinesection.logisticraft.gui.elements;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.sinesection.logisticraft.api.gui.IGuiElement;
import com.sinesection.logisticraft.api.gui.IWindowElement;
import com.sinesection.logisticraft.api.gui.events.GuiEventDestination;
import com.sinesection.logisticraft.gui.elements.layouts.VerticalLayout;
import com.sinesection.logisticraft.gui.events.EventValueChanged;

/**
 * A element list with selectable elements.
 */
public class ElementList<V> extends VerticalLayout {
	private final Map<V, IGuiElement> allOptions = new LinkedHashMap<>();
	private final Map<V, IGuiElement> visibleOptions = new LinkedHashMap<>();
	private final BiFunction<V, ElementList<?>, IGuiElement> optionFactory;
	@Nullable
	private final V defaultValue;
	@Nullable
	private V value;
	@Nullable
	private Predicate<V> validator;

	public ElementList(int xPos, int yPos, int width, BiFunction<V, ElementList<?>, IGuiElement> optionFactory, @Nullable V defaultValue) {
		super(xPos, yPos, width);
		this.optionFactory = optionFactory;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	@Nullable
	public V getCurrentValue() {
		return this.value;
	}

	public void setCurrentValue(@Nullable final V value) {
		if (value == this.value) {
			return;
		}
		this.value = value;
		/*
		 * if (this.optionWidgets.containsKey(value)) { final IWidget child =
		 * this.optionWidgets.get(value);
		 * this.parent.ensureVisible(child.getYPos(), child.getYPos() +
		 * child.getHeight(), this.getHeight()); }
		 */
		IWindowElement window = getWindow();
		window.postEvent(new EventValueChanged<Object>(this, value), GuiEventDestination.ALL);
	}

	public void updateVisibleOptions() {
		elements.clear();
		setHeight(0);
		visibleOptions.clear();

		for (Map.Entry<V, IGuiElement> entry : this.allOptions.entrySet()) {
			if (isVisible(entry.getKey())) {
				add(entry.getValue());
				visibleOptions.put(entry.getKey(), entry.getValue());
			} else {
				entry.getValue().setYPosition(0);
			}
		}
		setCurrentValue(this.getCurrentValue());
	}

	public boolean isVisible(V value) {
		return validator == null || validator.test(value);
	}

	public void setValidator(Predicate<V> validator) {
		if (this.validator != validator) {
			this.validator = validator;
			updateVisibleOptions();
		}
	}

	public int getIndexOf(@Nullable V value) {
		if (value == null) {
			return -1;
		}
		int index = 0;
		for (V option : this.getOptions()) {
			if (option.equals(value)) {
				return index;
			}
			++index;
		}
		return -1;
	}

	public int getCurrentIndex() {
		return this.getIndexOf(getCurrentValue());
	}

	public void setIndex(int currentIndex) {
		int index = 0;
		for (V option : this.getOptions()) {
			if (index == currentIndex) {
				this.setCurrentValue(option);
				return;
			}
			++index;
		}
		this.setCurrentValue(defaultValue);
	}

	public Collection<V> getOptions() {
		return visibleOptions.keySet();
	}

	public void setOptions(Collection<V> options) {
		clear();
		allOptions.clear();
		for (V option : options) {
			IGuiElement element = optionFactory.apply(option, this);
			allOptions.put(option, element);
		}
		updateVisibleOptions();
	}
}