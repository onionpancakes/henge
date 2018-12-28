import React from 'react';
import renderer from 'react-test-renderer';

import { com } from "./components";

const {
    Widget,
    WidgetNested,
    WidgetKeyword,
    WidgetJSProps,
    WidgetMapProps,
    WidgetMapPropsMapClasses,
    WidgetFor,
    WidgetFragment,
    WidgetOther,
    WidgetSkip,
 } = com.onionpancakes.henge.testjs.components;

it("Widget", () => {
    let component = renderer.create(
        <Widget></Widget>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetNested", () => {
    let component = renderer.create(
        <WidgetNested></WidgetNested>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetKeyword", () => {
    let component = renderer.create(
        <WidgetKeyword></WidgetKeyword>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetJSProps", () => {
    let component = renderer.create(
        <WidgetJSProps></WidgetJSProps>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetMapProps", () => {
    let component = renderer.create(
        <WidgetMapProps></WidgetMapProps>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetMapPropsMapClasses", () => {
    let component = renderer.create(
        <WidgetMapPropsMapClasses></WidgetMapPropsMapClasses>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetFor", () => {
    let component = renderer.create(
        <WidgetFor></WidgetFor>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetFragment", () => {
    let component = renderer.create(
        <div>
            <WidgetFragment></WidgetFragment>
        </div>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetOther", () => {
    let component = renderer.create(
        <WidgetOther></WidgetOther>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetSkip", () => {
    let component = renderer.create(
        <WidgetSkip></WidgetSkip>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});
