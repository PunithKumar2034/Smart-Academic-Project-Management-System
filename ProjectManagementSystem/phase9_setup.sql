-- Phase 9: Database Setup for Project Tracking

-- 1. Create Submissions Table
CREATE TABLE IF NOT EXISTS public.submissions (
    id SERIAL PRIMARY KEY,
    deadline_id INTEGER REFERENCES public.deadlines(id) ON DELETE CASCADE,
    team_id INTEGER REFERENCES public.teams(id) ON DELETE CASCADE,
    student_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    grade VARCHAR(20),
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. Enable RLS
ALTER TABLE public.submissions ENABLE ROW LEVEL SECURITY;

-- 3. RLS Policies for Submissions
-- Students can insert submissions for their team
CREATE POLICY "Students can submit work" ON public.submissions
FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM team_members WHERE team_members.team_id = submissions.team_id AND team_members.student_id = auth.uid())
);

-- Students can view their team's submissions
CREATE POLICY "Students can view team submissions" ON public.submissions
FOR SELECT USING (
    EXISTS (SELECT 1 FROM team_members WHERE team_members.team_id = submissions.team_id AND team_members.student_id = auth.uid())
);

-- Faculty can view submissions for their projects
CREATE POLICY "Faculty can view project submissions" ON public.submissions
FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM deadlines 
        JOIN projects ON deadlines.project_id = projects.id 
        WHERE deadlines.id = submissions.deadline_id AND projects.faculty_id = auth.uid()
    )
);

-- Faculty can update grades
CREATE POLICY "Faculty can grade submissions" ON public.submissions
FOR UPDATE USING (
    EXISTS (
        SELECT 1 FROM deadlines 
        JOIN projects ON deadlines.project_id = projects.id 
        WHERE deadlines.id = submissions.deadline_id AND projects.faculty_id = auth.uid()
    )
);
