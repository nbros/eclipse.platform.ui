/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
*     Simon Scholz <simon.scholz@vogella.com> - Bug 544471
******************************************************************************/
package org.eclipse.jface.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract factory for controls. Factories for controls that inherit from
 * Control should extend this factory to handle the properties of Control
 * itself, like enabled or tool tip.
 *
 * @param <F>
 * @param <C>
 */
public abstract class ControlFactory<F extends ControlFactory<?, ?>, C extends Control> {
	private Class<F> factoryClass;

	private Function<Composite, C> controlCreator;

	private List<Property<C>> properties = new ArrayList<>();

	/**
	 * @param factoryClass
	 * @param controlCreator
	 */
	protected ControlFactory(Class<F> factoryClass, Function<Composite, C> controlCreator) {
		this.factoryClass = factoryClass;
		this.controlCreator = controlCreator;
	}

	/**
	 * Sets the tool tip.
	 *
	 * @param tooltipText
	 * @return this
	 */
	public F tooltip(String tooltipText) {
		addProperty(c -> c.setToolTipText(tooltipText));
		return cast(this);
	}

	/**
	 * Sets the enabled state.
	 *
	 * @param enabled
	 * @return this
	 */
	public F enabled(boolean enabled) {
		addProperty(c -> c.setEnabled(enabled));
		return cast(this);
	}

	/**
	 * Sets a {@link Supplier}, which should always create a new instance of the
	 * layoutData in order to make this factory reusable, because each and every
	 * control needs its own unique layoutData.<br />
	 * {@link #layoutData(Object)} is sufficient if only one control should be
	 * created with this factory.
	 *
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false);
	 * ButtonFactory.newButton(SWT.PUSH).layoutData(gridDataFactory::create);
	 * </pre>
	 * </p>
	 *
	 * or without GridDataFactory:
	 *
	 * <p>
	 *
	 * <pre>
	 * ButtonFactory.newButton(SWT.PUSH).layoutData(() -> new GridData());
	 * </pre>
	 * </p>
	 *
	 * @param layoutDataSupplier {@link Supplier} creating a new layout data
	 *                           instance
	 * @return this
	 */
	public F layoutData(Supplier<Object> layoutDataSupplier) {
		addProperty(c -> c.setLayoutData(layoutDataSupplier.get()));
		return cast(this);
	}

	/**
	 * Sets a layout object, which is used for the setLayout method of the control.
	 * This method can be used if only one control should be created with this
	 * factory.<br />
	 * Use {@link #layoutData(Supplier)} if several controls with the same instance
	 * of factory should be created. LayoutData should be unique and they should not
	 * be shared among controls.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * GridData gd = new GridData(GridData.FILL_BOTH);
	 * ButtonFactory.newButton(SWT.PUSH).layoutData(gd);
	 * </pre>
	 * </p>
	 *
	 * @param layoutData
	 * @return this
	 */
	public F layoutData(Object layoutData) {
		addProperty(c -> c.setLayoutData(layoutData));
		return cast(this);
	}

	/**
	 * Sets the font.
	 *
	 * @param font
	 *
	 * @return this
	 */
	public F font(Font font) {
		addProperty(c -> c.setFont(font));
		return cast(this);
	}

	/**
	 * Casts to the concrete instance of factory class. Needs to be called by
	 * abstract factory classes.
	 *
	 * @param factory extending ControlFactory, usually "this"
	 * @return casted factory
	 */
	protected final F cast(ControlFactory<F, C> factory) {
		return factoryClass.cast(factory);
	}

	/**
	 * @param parent
	 * @return this
	 */
	public final C create(Composite parent) {
		C control = controlCreator.apply(parent);
		properties.forEach(p -> p.apply(control));
		return control;
	}

	/**
	 * Adds a property like image, text, enabled, listeners, ... to the control.
	 *
	 * <br/>
	 * Example:
	 *
	 * <pre>
	 * public LabelFactory text(String text) {
	 * 	addProperty(l -> l.setText(text));
	 * 	return this;
	 * }
	 * </pre>
	 *
	 * @param property usually a lambda
	 */
	protected final void addProperty(Property<C> property) {
		this.properties.add(property);
	}
}