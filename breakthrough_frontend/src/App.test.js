import { render, screen } from '@testing-library/react';
import App from './App';

test('renders learn react link', () => {
  render(<App />);
  const linkElement = screen.getByText(/Le serveur n'est pas connecté/i);
  expect(linkElement).toBeInTheDocument();
});
