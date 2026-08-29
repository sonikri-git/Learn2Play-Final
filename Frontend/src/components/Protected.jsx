import {Navigate} from 'react-router-dom'; export default function Protected({children}){return localStorage.getItem('accessToken')?<>{children}</>:<Navigate to="/login" replace/>}
