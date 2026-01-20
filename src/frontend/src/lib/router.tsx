import { createBrowserRouter } from "react-router-dom";
import HomePage from "../pages/HomePage";
import CartPage from "../pages/CartPage";
import LoginPage from "../pages/LoginPage";
import NotFoundPage from "../pages/NotFoundPage";
import ProductDetailPage from "../pages/ProductDetailPage";
import ExercisePage from "../pages/ExercisePage";

export const router = createBrowserRouter([
    {
        path: '/',
        element: <HomePage />,
    },
    {
        path: '/login',
        element: <LoginPage />,
    },
    {
        path: '/cart',
        element: <CartPage />,
    },
    {
        path: '/products/:id',
        element: <ProductDetailPage />
    },
    {
        path: '/exercise',
        element: <ExercisePage />
    },
    {
        path: '*',  // PHẢI Ở CUỐI CÙNG!
        element: <NotFoundPage />,
    }
])