Context & Goal

Generate a premium, modern Java GUI login interface. The layout and structural composition must be an exact replica of the provided reference design (left-side form area, wavy vertical dividing line, right-side background). However, the theme, color palette, and background image must be adapted to a cool, icy-blue pixel-art mountain landscape. Apply modern 3D-like shading (subtle neumorphism and glassmorphism) and advanced micro-interactions.

Global Styling & Background

Window Frame: Borderless, undecorated Java window with heavily rounded corners (e.g., 24px radius).

Background: Use the pixel-art icy mountain landscape as the base background.

Layout Split: Create a 55/45 split.

Right Side: Displays the mountain landscape clearly.

Left Side (Form Area): Covered by a deep, cool-blue frosted glass overlay (Glassmorphism). Use rgba(26, 37, 48, 0.85) with a backdrop blur effect.

Divider: The transition between the left dark area and the right background must feature a subtle, elegant wavy/curved bezier line stretching from top to bottom, featuring a faint, dashed/dotted light blue stroke (rgba(133, 161, 193, 0.3)).

Color Palette (Derived from Pixel Art Landscape)

Primary Background / Glass: Deep Navy/Charcoal Blue (#1A2530)

Secondary/Inputs: Slightly lighter Cool Blue (#243342)

Accent / Primary Action: Vibrant Icy Blue (#5C8EB8 to #85A1C1 gradient)

Text (Primary): Pure White (#FFFFFF)

Text (Secondary/Placeholder): Muted Frost Blue (#A0B4C8)

Success: Soft Mint Green (#4E9F76)

Error: Muted Rose/Red (#D9534F)

Component Breakdown

Apply subtle 3D shading to all components: a soft drop shadow (rgba(0,0,0,0.4)) for depth, and a faint 1px top-inner border (rgba(255,255,255,0.1)) for a premium beveled/3D edge.

1. Branding Area (Top Left)

App Logo: A sleek, minimalistic geometric icon using the Vibrant Icy Blue accent color.

App Title: Text "SYNORA". Font: Modern Sans-Serif (e.g., Poppins or Montserrat), Extra Bold, 32px, Pure White.

App Subtitle: Text "Project Management System". Font: Regular, 14px, Muted Frost Blue, positioned directly below the title.

2. Login Form Components (Vertically Centered on Left)

Header: "START FOR FREE" (small, uppercase, tracking-wide) and "Create new account." (Large, bold, with a blue dot at the end) - Adapt text to "Welcome Back" / "Login to your account." if strictly a login page, but keep the typography style of the reference.

Email Input Field:

Text box, placeholder "Email address".

Background: #243342 with 3D inset shadow (looks slightly pressed in).

Rounded corners (12px). Includes a small email icon on the right side.

Password Input Field:

Hidden-text box, placeholder "Password".

Same styling as the email field. Includes a visibility toggle icon (eye) on the right side.

Role Selection Switch (Faculty / Student):

Crucial Styling: Must be physically smaller (about 80% of the width and slightly shorter in height) than the email/password fields to look highly aesthetic and perfectly proportioned.

Pill-shaped segmented toggle. Background #1A2530, inner shadow.

The active state is a floating pill inside the track using the Accent Icy Blue, providing a 3D raised button look.

Primary Login Button:

Large, solid button matching the width of the input fields. Text "LOGIN".

Color: Vibrant Icy Blue gradient with a strong, glowing drop-shadow (rgba(92, 142, 184, 0.5)) to make it pop off the screen. Highly rounded (pill shape).

Status Message Area:

Dynamic text block below the button. Invisible by default. Uses 12px font (Red for error, Green for success).

Forgot Password Link:

Bottom-aligned, 13px Muted Frost Blue text, underlined.

Advanced Animations & Interactions

Implement these using Java animation timers or appropriate layout transition libraries:

Entrance Animation:

On launch, the application window fades in from 0% to 100% opacity over 600ms.

The form elements slide in sequentially from the left (staggered delay of 50ms per element, easing out).

Input Field Hover & Focus (3D Reactivity):

Hover: The inset shadow slightly lightens.

Focus: A smooth transition applies a glowing 2px border in #5C8EB8. The placeholder text smoothly glides up and shrinks to become a top label.

Role Toggle Switch:

Clicking triggers a fluid, spring-physics sliding animation of the active pill background from "Faculty" to "Student". Text color inside the pill cross-fades from muted to white.

Button Interactions:

Hover: The button physically scales up slightly (1.03x) and the glowing drop shadow expands (Neumorphic pop effect).

Click: A rapid scale-down (0.97x) and scale-up (bounce), accompanied by a circular ripple effect radiating from the click point.

Processing: Upon clicking, the "LOGIN" text fades out, replaced by a smooth, spinning 3D loading ring inside the button.

Background Parallax:

If possible within the framework, attach a subtle mouse-move listener that shifts the right-side pixel art background slightly (parallax effect) opposite to the mouse direction, adding deep immersion.