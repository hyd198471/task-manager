import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import App from '../App';

describe('App basic render', () => {
  it('renders heading', () => {
    render(React.createElement(App));
    expect(screen.getByText(/Task Manager/i)).toBeInTheDocument();
  });
});
