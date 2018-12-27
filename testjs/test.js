import React from 'react';
import renderer from 'react-test-renderer';

import { com } from "./components";

const { Widget } = com.onionpancakes.henge.testjs.components;

it("Fails", () => {
    let component = renderer.create(
        <Widget></Widget>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});
