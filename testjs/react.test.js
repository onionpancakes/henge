import React from 'react';
import renderer from 'react-test-renderer';

it("lowercase dot access are user components", () => {
    let foo = {
        bar() {
            return <div>"bar"</div>;
        }
    };
    let component = renderer.create(
        <foo.bar></foo.bar>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});
