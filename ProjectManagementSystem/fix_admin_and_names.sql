-- 1. Rename users from "User" to descriptive names
UPDATE public.users SET name = 'Dr. Sarah Smith' WHERE email = 'smith@example.com';
UPDATE public.users SET name = 'Dr. Robert Jones' WHERE email = 'jones@example.com';
UPDATE public.users SET name = 'Dr. Emily Brown' WHERE email = 'brown@example.com';
UPDATE public.users SET name = 'Alice Cooper' WHERE email = 'student1@example.com';
UPDATE public.users SET name = 'Bob Marley' WHERE email = 'student2@example.com';
UPDATE public.users SET name = 'Charlie Day' WHERE email = 'student3@example.com';
UPDATE public.users SET name = 'Diana Prince' WHERE email = 'student4@example.com';
UPDATE public.users SET name = 'Ethan Hunt' WHERE email = 'student5@example.com';
UPDATE public.users SET name = 'Fiona Gallagher' WHERE email = 'student6@example.com';
UPDATE public.users SET name = 'George Costanza' WHERE email = 'student7@example.com';
UPDATE public.users SET name = 'Hannah Baker' WHERE email = 'student8@example.com';
UPDATE public.users SET name = 'Ian McKellen' WHERE email = 'student9@example.com';
UPDATE public.users SET name = 'Julia Roberts' WHERE email = 'student10@example.com';

-- 2. Update RLS Policies to allow Admin bypass
-- Function to check if current user is admin
CREATE OR REPLACE FUNCTION is_admin() 
RETURNS BOOLEAN AS $$
BEGIN
  RETURN EXISTS (
    SELECT 1 FROM public.users 
    WHERE id = auth.uid() AND role = 'admin'
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Update Policies for project_applications
DROP POLICY IF EXISTS "Faculty can update application status" ON public.project_applications;
CREATE POLICY "Faculty/Admin can update application status" ON public.project_applications
FOR UPDATE USING (
    is_admin() OR
    EXISTS (SELECT 1 FROM projects WHERE projects.id = project_applications.project_id AND projects.faculty_id = auth.uid())
);

DROP POLICY IF EXISTS "Faculty can view applications for their projects" ON public.project_applications;
CREATE POLICY "Faculty/Admin can view applications" ON public.project_applications
FOR SELECT USING (
    is_admin() OR
    EXISTS (SELECT 1 FROM projects WHERE projects.id = project_applications.project_id AND projects.faculty_id = auth.uid()) OR
    auth.uid() = student_id
);

-- Update Policies for projects
DROP POLICY IF EXISTS "Faculty can update their own projects" ON public.projects;
CREATE POLICY "Faculty/Admin can update projects" ON public.projects
FOR UPDATE USING (
    is_admin() OR faculty_id = auth.uid()
);

DROP POLICY IF EXISTS "Faculty can delete their own projects" ON public.projects;
CREATE POLICY "Faculty/Admin can delete projects" ON public.projects
FOR DELETE USING (
    is_admin() OR faculty_id = auth.uid()
);

-- Enable Admin to manage deadlines too
DROP POLICY IF EXISTS "Faculty can manage deadlines" ON public.deadlines;
CREATE POLICY "Faculty/Admin can manage deadlines" ON public.deadlines
FOR ALL USING (
    is_admin() OR 
    EXISTS (SELECT 1 FROM projects WHERE projects.id = deadlines.project_id AND projects.faculty_id = auth.uid())
);
