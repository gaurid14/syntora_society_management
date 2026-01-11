import type { AgnosticRoute } from "@vaadin/hilla-file-router/types.js";
import { createRoute } from "@vaadin/hilla-file-router/runtime.js";
import * as Page0 from "../views/@index.js";
import * as Page1 from "../views/admin_register.js";
import * as Page2 from "../views/App.js";
import * as Page3 from "../views/forgot_password.js";
import * as Page4 from "../views/login.js";
import * as Page5 from "../views/logout.js";
import * as Page6 from "../views/society_register.js";
import * as Page7 from "../views/webadmin.js";
const routes: readonly AgnosticRoute[] = [
    createRoute("", Page0),
    createRoute("admin_register", Page1),
    createRoute("App", Page2),
    createRoute("forgot_password", Page3),
    createRoute("login", Page4),
    createRoute("logout", Page5),
    createRoute("society_register", Page6),
    createRoute("webadmin", Page7)
];
export default routes;
