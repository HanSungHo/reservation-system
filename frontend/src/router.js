
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);



import Mypage from "./components/Mypage"
import ReservationManager from "./components/ReservationManager"

import PaymentManager from "./components/PaymentManager"

import ManagementManager from "./components/ManagementManager"

import SeatManager from "./components/SeatManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [

            {
                path: '/Mypage',
                name: 'Mypage',
                component: Mypage
            },
            {
                path: '/Reservation',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/Payment',
                name: 'PaymentManager',
                component: PaymentManager
            },

            {
                path: '/Management',
                name: 'ManagementManager',
                component: ManagementManager
            },

            {
                path: '/Seat',
                name: 'SeatManager',
                component: SeatManager
            },



    ]
})
